package io.github.lujiafa.houtu.core.exception;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

public class ErrorResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

    final static String CLASSPATH_RESOURCE_PATTERN = "classpath*:";

    private List<String> fileExtensions = List.of(".properties", ".xml");
    private PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader);
        pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
    }


    @Override
    protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {
        if (!filename.startsWith(CLASSPATH_RESOURCE_PATTERN)) {
            return super.refreshProperties(filename, propHolder);
        }
        long refreshTimestamp = this.getCacheMillis() < 0L ? -1L : System.currentTimeMillis();
        try {
            Resource[] resources = resolveErrorResource(filename);
            if (resources != null && resources.length > 0) {
                Properties properties = new Properties();
                long fileTimestamp = -1L;
                try {
                    for (Resource resource : resources) {
                        IOException ex;
                        if (this.getCacheMillis() >= 0L) {
                            try {
                                if (resource.lastModified() > fileTimestamp) {
                                    fileTimestamp = resource.lastModified();
                                }
                            } catch (IOException var10) {
                                ex = var10;
                                if (this.logger.isDebugEnabled()) {
                                    this.logger.debug("" + resource + " could not be resolved in the file system - assuming that it hasn't changed", ex);
                                }
                                fileTimestamp = -1L;
                            }
                        }
                        Properties props = this.loadProperties(resource, filename);
                        properties.putAll(props);
                    }
                    if (this.getCacheMillis() >= 0L && propHolder != null && propHolder.getFileTimestamp() == fileTimestamp) {
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("Re-caching properties for filename [" + filename + "] - file hasn't been modified");
                        }

                        propHolder.setRefreshTimestamp(refreshTimestamp);
                        return propHolder;
                    }
                    propHolder = new PropertiesHolder(properties, fileTimestamp);
                } catch (Exception ex) {
                    if (this.logger.isWarnEnabled()) {
                        this.logger.warn("Could not parse properties file [" + filename + "]", ex);
                    }

                    propHolder = new PropertiesHolder();
                }
            } else {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("No properties file found for [" + filename + "]");
                }

                propHolder = new PropertiesHolder();
            }
            propHolder.setRefreshTimestamp(refreshTimestamp);
            getSuperCacheProperties().put(filename, propHolder);
            return propHolder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ConcurrentMap<String, PropertiesHolder> getSuperCacheProperties() throws NoSuchFieldException, IllegalAccessException {
        Field field = ReloadableResourceBundleMessageSource.class.getDeclaredField("cachedProperties");
        field.setAccessible(true);
        return (ConcurrentMap<String, PropertiesHolder>) field.get(this);
    }

    public Resource[] resolveErrorResource(String filename) throws IOException {
        List<Resource> resources = new ArrayList<>();
        for (String fileExtension : fileExtensions) {
            Resource[] array = pathMatchingResourcePatternResolver.getResources(filename + fileExtension);
            for (Resource resource : array) {
                if (resource.exists()) {
                    resources.add(resource);
                }
            }
        }
        return resources.toArray(new Resource[0]);
    }
}
