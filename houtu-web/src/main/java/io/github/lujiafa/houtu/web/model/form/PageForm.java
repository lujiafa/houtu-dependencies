package io.github.lujiafa.houtu.web.model.form;

import io.github.lujiafa.houtu.web.model.BaseForm;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
public class PageForm extends BaseForm {

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
