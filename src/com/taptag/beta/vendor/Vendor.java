package com.taptag.beta.vendor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.annotate.JsonRootName;


import android.util.Log;

import com.taptag.beta.location.TagAddress;

@SuppressWarnings("serial")
@JsonRootName(value = "vendor")
public class Vendor implements Comparable<Vendor>, Serializable {

	private int id;
	private String name;
	private int company_id;
	private TagAddress address;

	// //private String category;

	public Vendor() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCompanyId() {
		return company_id;
	}

	public void setCompany_id(int company_id) {
		this.company_id = company_id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(TagAddress address) {
		this.address = address;
	}

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

	public int compareTo(Vendor arg0) {
		String thisName = this.getName();
		String otherName = arg0.getName();
		return thisName.compareTo(otherName);
	}
}
