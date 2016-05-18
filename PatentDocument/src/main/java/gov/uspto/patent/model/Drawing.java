package gov.uspto.patent.model;

public class Drawing {
	private String id;
	private String num;
	private String file;
	private DrawingMetadata metadata;

	Drawing(String id, String num, String file){
		this.setId(id);
		this.setNum(num);
		this.setFile(file);
	}
	
	public void setMetadata(DrawingMetadata metadata){
		this.metadata = metadata;
	}
	
	public DrawingMetadata getMetadata(){
		return metadata;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "Drawing [id=" + id + ", num=" + num + ", file=" + file + ", metadata=" + metadata + "]";
	}

}
