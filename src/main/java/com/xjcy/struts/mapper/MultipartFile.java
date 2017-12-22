package com.xjcy.struts.mapper;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tomcat.util.http.fileupload.FileItemStream;
import com.xjcy.util.StringUtils;

public class MultipartFile
{
	String fileName;
	String extension;
	Long size;
	private InputStream inputStream;
	private String fieldName;

	public Long getSize()
	{
		return size;
	}

	public MultipartFile(String fileName, String fieldName, InputStream inputStream, Long size)
	{
		this.fileName = fileName;
		this.fieldName = fieldName;
		this.inputStream = inputStream;
		this.size = size;
		if (!StringUtils.isEmpty(fileName))
		{
			int last = fileName.lastIndexOf(".");
			if (last > 0)
				this.extension = fileName.substring(last);
			else
				this.extension = "";
		}
	}

	public MultipartFile(FileItemStream stream) throws IOException
	{
		this(stream.getName(), stream.getFieldName(), stream.openStream(),
				Long.parseLong(stream.openStream().available() + ""));
	}

	public InputStream getInputStream()
	{
		return inputStream;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getExtension()
	{
		return extension;
	}

	public boolean isPic()
	{
		if (StringUtils.isEmpty(extension))
			return false;
		return ".jpg".equals(extension) || ".png".equals(extension) || ".bmp".equals(extension)
				|| ".gif".equals(extension) || ".jpeg".equals(extension);
	}
	
	public boolean isVedio()
	{
		if (StringUtils.isEmpty(extension))
			return false;
		return ".avi".equals(extension) || ".mp4".equals(extension) || ".wmv".equals(extension)
				|| ".flv".equals(extension) || ".mov".equals(extension);
	}

	public void clear()
	{
		this.fileName = null;
		this.extension = null;
		this.size = null;
		this.inputStream = null;
	}

	public String getFieldName()
	{
		return fieldName;
	}
}
