package io.github.lujiafa.houtu.web.model.vo;

import io.github.lujiafa.houtu.util.common.BeanUtils;
import io.github.lujiafa.houtu.web.model.BaseDTO;
import io.github.lujiafa.houtu.web.model.dto.PageDataDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
public class PageDataVO<V> extends BaseDTO {

	private static final long serialVersionUID = 1L;
	
	private static final long DEFAULT_PAGE_SIZE = 10;
	private static final long DEFAULT_CURRENT_PAGE = 1;
	
	private Long pageSize; // 每页显示数,默认值为15
	private Long currentPage; // 当前页数,默认值为1
	private Long totalPages; // 总页数
	private Long totalRecords; // 总记录数
	protected List<V> records; // 存储查询返回result数据集合

	protected PageDataVO() {
	}

	/**
	 * @Title:getTotalPages
	 * @Description:计算总页数，并获取总页数
	 * @return Long
	 */
	public Long getTotalPages() {
		if (null != this.totalRecords && null != this.pageSize && 0 != this.pageSize) {
			this.totalPages = this.totalRecords % this.pageSize == 0 ? this.totalRecords / this.pageSize
					: this.totalRecords / this.pageSize + 1;
		} else {
			this.totalPages = 0L;
		}
		return this.totalPages;
	}

	/**
	 * @Title:getCurrentPage
	 * @Description: 判断当前页数是否符合逻辑，获取当前页数
	 * @return Long
	 */
	public Long getCurrentPage() {
		if (null == currentPage) {
			currentPage = DEFAULT_CURRENT_PAGE;
		} else if (currentPage <= 1) {
			currentPage = DEFAULT_CURRENT_PAGE;
		} else if (null != this.totalRecords && null != this.pageSize && 0 != this.pageSize
				&& currentPage >= getTotalPages()) {
			currentPage = getTotalPages();
		}
		return currentPage;
	}

	public Long getPageSize() {
		if (pageSize == null) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		return pageSize;
	}

	public List<V> getRecords() {
		if (records == null) {
			return Collections.emptyList();
		}
		return records;
	}
	
	public Long getTotalRecords() {
		return this.totalRecords;
	}

	public static <V> PageDataVO<V> build(PageDataDTO<?, ?> pageDataDTO, Class<V> dataClass) {
		PageDataVO<V> pageDataVO = new PageDataVO<V>();
		if (pageDataDTO == null || dataClass == null) {
			return pageDataVO;
		}
		pageDataVO.currentPage = pageDataDTO.getCurrentPage();
		pageDataVO.pageSize = pageDataDTO.getPageSize();
		pageDataVO.totalRecords = pageDataDTO.getTotalRecords();
		pageDataVO.records = BeanUtils.smartCopyProperties(pageDataDTO.getRecords(), dataClass);
		return pageDataVO;
	}
	
	public static <V> PageDataVO<V> build(long currentPage, long pageSize, long totalRecords, List<V> records) {
		PageDataVO<V> pageDataVO = new PageDataVO<V>();
		if (records == null) {
			return pageDataVO;
		}
		pageDataVO.currentPage = currentPage;
		pageDataVO.pageSize = pageSize;
		pageDataVO.totalRecords = totalRecords;
		pageDataVO.records = records;
		return pageDataVO;
	}
	
	public static <V> PageDataVO<V> empty() {
		PageDataVO<V> pageDataVO = new PageDataVO<V>();
		pageDataVO.currentPage = 1L;
		pageDataVO.pageSize = 10L;
		pageDataVO.totalRecords = 0L;
		pageDataVO.records = new ArrayList<>();
		return pageDataVO;
	}

}