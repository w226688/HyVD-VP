package lucee.commons.lang;

public interface ClassLoaderDefault {
	public Class<?> loadClass(String name, boolean resolve, Class<?> defaultValue);
}
