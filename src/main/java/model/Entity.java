package model;

public class Entity {
	private final String name;
	private final String url;
	private final String entityName;
	private final String categoryFolder;

	public Entity(String name, String url, String entityName, String categoryFolder) {
		super();
		this.name = name;
		this.url = url;
		this.entityName = entityName;
		this.categoryFolder = categoryFolder;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getEntityName() {
		return entityName;
	}

	public String getCategoryFolder() {
		return categoryFolder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((categoryFolder == null) ? 0 : categoryFolder.hashCode());
		result = prime * result + ((entityName == null) ? 0 : entityName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (categoryFolder == null) {
			if (other.categoryFolder != null)
				return false;
		} else if (!categoryFolder.equals(other.categoryFolder))
			return false;
		if (entityName == null) {
			if (other.entityName != null)
				return false;
		} else if (!entityName.equals(other.entityName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Entity [name=" + name + ", url=" + url + ", entityName=" + entityName + ", categoryFolder="
				+ categoryFolder + "]";
	}

}
