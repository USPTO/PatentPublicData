package gov.uspto.patent.model;

public class Image {
	private String id;
	private String file;
	private String format;
	
	public Image(String id, String file, String format){
		this.setId(id);
		this.setFile(file);
		this.setFormat(format);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public String toString() {
		return "Image [id=" + id + ", file=" + file + ", format=" + format + "]";
	}
}
