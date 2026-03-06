package io.github.lujiafa.houtu.web.model.vo;

/**
 * @date 2019年5月29日
 * @author jonlu
 */
public class PageDataExtVO<D, V> extends PageDataVO<V> {

	private static final long serialVersionUID = 1L;

	private D data;

	public D getData() {
		return data;
	}

	public void setData(D data) {
		this.data = data;
	}

}