package com.nikolay.vb.container;

import java.io.Serializable;

public class DrawController implements Serializable, IDrawController {
	private Boolean erase = false;
	@Override
	public Boolean getErase() {
		// TODO Auto-generated method stub
		return erase;
	}

	@Override
	public void setErase(Boolean erase) {
		// TODO Auto-generated method stub
		this.erase = erase;
	}

}
