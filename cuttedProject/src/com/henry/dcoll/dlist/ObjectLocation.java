package com.henry.dcoll.dlist;

public class ObjectLocation {
	private String owner;
	private String space;
	private String list;
	private Integer index;

	public ObjectLocation(String owner, String space, String list, Integer index) {
		super();
		this.owner = owner;
		this.space = space;
		this.list = list;
		this.index = index;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public String getList() {
		return list;
	}

	public void setList(String list) {
		this.list = list;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public void increaseIndex() {
		index += 1;
	}
	
	public void decreaseIndex() {
		index -= 1;
	}

	@Override
	public String toString() {
		return owner + ":" + space + ":" + list + ":" + index;
	}
}
