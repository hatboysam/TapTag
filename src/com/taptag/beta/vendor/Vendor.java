package com.taptag.beta.vendor;

import java.io.Serializable;

import com.taptag.beta.location.TagAddress;

@SuppressWarnings("serial")
public class Vendor implements Comparable<Vendor>, Serializable {
	
	private String name;
	private TagAddress address;
	
	public Vendor(String name, TagAddress address) {
		this.name = name;
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public TagAddress getAddress() {
		return address;
	}

	@Override
	public int compareTo(Vendor arg0) {
		String thisName = this.getName();
		String otherName = arg0.getName();
		return thisName.compareTo(otherName);
	}

}
