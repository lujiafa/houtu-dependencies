package io.github.lujiafa.houtu.web.model.dto;

import io.github.lujiafa.houtu.web.model.BaseDTO;

import java.util.Collections;
import java.util.List;

/**
 * @date 2019年7月2日
 * @author jonlu
 * @Description 分页查询数据载体DTO
 */
public class PageDataDTO<R, V> extends BaseDTO {

	private static final long serialVersionUID = 1L;
	
	private static final long DEFAULT_PAGE_SIZE = 10;
	private static final long DEFAULT_CURRENT_PAGE = 1;

	// 分页大小（缺省默认10）
	private Long pageSize;
	// 当前页码（缺省默认1）
	private Long currentPage;
	// 附加参数
	private R param;
	// 是否需要查询总记录数并计算总页数。次字段仅用于辅助查询，目的是非必要查询时不做单独的总量统计查询
	private boolean countPage = true;

	// 分页记录数据
	private List<V> records;
	// 页码总数
	private Long totalPages;
	// 页记录数
	private Long totalRecords;

	/**
	 * @Title:PageInfo
	 * @Description: 无参构造函数
	 */
	public PageDataDTO() {
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
		if (null == currentPage || currentPage < 1) {
			return DEFAULT_CURRENT_PAGE;
		} else if (null != this.totalRecords && null != this.pageSize && 0 != this.pageSize
				&& currentPage >= getTotalPages()) {
			return getTotalPages();
		}
		return currentPage;
	}

	public Long getPageSize() {
		if (pageSize == null) {
			return DEFAULT_PAGE_SIZE;
		}
		return pageSize;
	}

	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}

	public void setCurrentPage(Long currentPage) {
		this.currentPage = currentPage;
	}

	public Long getTotalRecords() {
		return this.totalRecords;
	}

	public void setTotalRecords(Long totalRecords) {
		this.totalRecords = totalRecords;
	}

	public R getParam() {
		return param;
	}

	public void setParam(R param) {
		this.param = param;
	}
	
	public boolean isCountPage() {
		return countPage;
	}
	
	public void setCountPage(boolean countPage) {
		this.countPage = countPage;
	}

	public List<V> getRecords() {
		if (records == null) {
			return Collections.emptyList();
		}
		return records;
	}

	public void setRecords(List<V> records) {
		this.records = records;
	}

}