package com.houtu.web.model.dto;

import com.houtu.web.model.BaseDTO;


/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2021年3月18日
 * @Description 分页查询DTO
 */
public class PageQueryDTO extends BaseDTO {

	private static final long serialVersionUID = 1L;

	private static final long DEFAULT_CURRENT_PAGE = 1;
	private static final long DEFAULT_PAGE_SIZE = 10;

	private Long currentPage;
	private Long pageSize;

	public Long getCurrentPage() {
		if (null == currentPage || currentPage < 1) {
			return DEFAULT_CURRENT_PAGE;
		}
		return currentPage;
	}

	public void setCurrentPage(Long currentPage) {
		this.currentPage = currentPage;
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

}
