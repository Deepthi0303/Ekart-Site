package com.jsp.ekart.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;

@Component
public class CloudinaryHelper {
	@Value("${cloudinary.cloud}")
	private String cloudname;
	@Value("${cloudinary.key}")
	private String key;
	@Value("${cloudinary.secret}")
	private String secret;
	public String saveToCloudinary(MultipartFile file) throws IOException
	{
//		CLOUDINARY_URL=cloudinary://324948985713533:wl6IXbjuRsYDSHs9eJTVk3fh1w0@dd4s0uhxx
//		"cloudinary://"+key+":"+secret+"@"+cloudname
		Cloudinary cloudinary = new Cloudinary("cloudinary://324948985713533:wl6IXbjuRsYDSHs9eJTVk3fh1w0@dd4s0uhxx");
		Map<String, Object> uploadOptions = new HashMap<>();
		uploadOptions.put("folder", "Products");
		Map map = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
		return (String) map.get("url");
	}
}

