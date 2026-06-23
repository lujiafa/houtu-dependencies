package io.github.lujiafa.houtu.websecurity.prop;

import io.github.lujiafa.houtu.websecurity.constant.SecurityConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = SignProperties.PREFIX)
public class SignProperties {

    public static final String PREFIX = "houtu.web.sign";

    private String signKey;

    /**
     * 签名字段名称（请求头或请求参数中的键名），默认 sign
     */
    private String signName = SecurityConstant.PARAM_SIGNATURE_NAME;

    /**
     * 签名数据来源，默认两者都取（先取请求头，取不到再取请求参数）
     */
    private Source source = Source.BOTH;

    /**
     * 附加必填参数列表，结合 signSource 取值并参与签名，默认 nonce + timestamp；配置为空列表表示无附加必填参数
     */
    private List<String> additionalParams = new ArrayList<>(Arrays.asList(
            SecurityConstant.PARAM_NONCE_NAME,
            SecurityConstant.PARAM_TIMESTAMP_NAME));

    public String getSignKey() {
        return signKey;
    }

    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public List<String> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(List<String> additionalParams) {
        this.additionalParams = additionalParams;
    }
}
