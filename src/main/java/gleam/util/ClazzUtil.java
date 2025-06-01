package gleam.util;

import static java.util.Locale.ENGLISH;

import java.beans.Transient;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.util.annotation.NonSingleton;

/**
 * 类工具类
 * 
 * @author hdh
 *
 */
public class ClazzUtil {

	private final static Logger logger = LoggerFactory.getLogger(ClazzUtil.class);

	public static final String FOUNDATION_PACKAGE_NAME = "gleam";
	public static final String GAME_PACKAGE_NAME = "com.game";
	public static final String CLASS_FILE_SUBFIX = ".class";

	/**
	 * 获取该类的所有父类<br>
	 * 不包括Object.class
	 * 
	 * @param type
	 * @return
	 */
	public static List<Class<?>> getAllSuperClasses(Class<?> type) {
		List<Class<?>> list = new ArrayList<>();
		Class<?> superC = type;
		while (!superC.equals(Object.class)) {
			list.add(superC);
			superC = superC.getSuperclass();
		}
		return list;
	}

	/**
	 * 获取所有带有getset方法的参数
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Field> getBeanFields(Class<?> clazz) {
		Map<String, Field> fieldMap = new HashMap<>();
		Class<?> tmpClazz = clazz;
		while (tmpClazz != null && !tmpClazz.equals(Object.class)) {
			Field[] fields = tmpClazz.getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				if (fieldMap.containsKey(fieldName)) {
					// 子类已有同名参数
					continue;
				}
				int mod = field.getModifiers();
				if (Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
					// 被final static transient 关键字修饰的忽略
					continue;
				}
				// 判断是否存在对应getset方法
				Method writeMethod = getWriteMethod(clazz, field);
				if (writeMethod == null) {
					continue;
				}
				Method readMethod = getReadMethod(clazz, field);
				if (readMethod == null) {
					// 无对应getset方法则跳过
					continue;
				}
				if (writeMethod.isAnnotationPresent(Transient.class)
						|| readMethod.isAnnotationPresent(Transient.class)) {
					// getset中有Transient注解的忽略
					continue;
				}
				fieldMap.put(fieldName, field);
			}
			tmpClazz = tmpClazz.getSuperclass();
		}
		if (fieldMap.isEmpty()) {
			return Collections.emptyList();
		}
		List<Field> fields = new ArrayList<>(fieldMap.values());
		return fields;
	}

	/**
	 * 获取该类的所有参数<br>
	 * 含父类参数的私有参数
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> tmpClazz : getAllSuperClasses(clazz)) {
			if (!tmpClazz.equals(Object.class)) {
				fields.addAll(Arrays.asList(tmpClazz.getDeclaredFields()));
			}
		}
		return fields;
	}

	/**
	 * 获取该类的非静态参数列表<br>
	 * 含父类的私有参数
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFieldsWithoutStatic(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> tmpClazz : getAllSuperClasses(clazz)) {
			if (tmpClazz.equals(Object.class)) {
				continue;
			}
			Field[] tmpFields = tmpClazz.getDeclaredFields();
			if (tmpFields == null || tmpFields.length <= 0) {
				continue;
			}
			for (Field field : tmpFields) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * 获取该对象的所有参数<br>
	 * 参数名中的.转义为&#46; 用于html页面展示
	 * 
	 * @param obj
	 * @return <参数名,值>
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Map<String, Object> getFieldValueMap(Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> map = new LinkedHashMap<>();
		if (obj instanceof Collection collection) {
			int index = 0;
			for (Object o : collection) {
				map.put("[" + String.valueOf(index) + "]", o);
				index++;
			}
		} else if (obj instanceof Iterator iter) {
			int index = 0;
			while (iter.hasNext()) {
				Object o = iter.next();
				map.put("[" + String.valueOf(index) + "]", o);
				index++;
			}
		} else if (obj instanceof Enumeration e) {
			int index = 0;
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				map.put("[" + String.valueOf(index) + "]", o);
				index++;
			}

		} else if (obj.getClass().isArray()) {
			for (int index = 0; index < Array.getLength(obj); index++) {
				Object o = Array.get(obj, index);
				map.put("[" + String.valueOf(index) + "]", o);
				index++;
			}
		} else if (obj instanceof Map<?, ?> m) {
			for (Entry<?, ?> entry : m.entrySet()) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				String k = "[" + String.valueOf(key) + "]";
				// 因使用了.进行分割 此处需要将.转义
				k = k.replace(".", "&#46;");
				map.put(k, value);
			}
		} else {
			List<Field> fields = ClazzUtil.getFields(obj.getClass());
			for (Field field : fields) {
				String cls = "";
				if (Modifier.isStatic(field.getModifiers())) {
					cls = "Static";
				}
				field.setAccessible(true);
				if (!cls.equals("")) {
					map.put(field.getName() + " # " + cls, field.get(obj));
				} else {
					map.put(field.getName(), field.get(obj));
				}
			}
		}
		return map;
	}

	/**
	 * 获取某个字段上的注解
	 * 
	 * @param object          字段实例
	 * @param annotationClazz 注解类型
	 * @param <T>             注解
	 * @return 目标注解
	 */
	public static <T extends Annotation> T getFieldAnnotation(Object object, Class<T> annotationClazz) {
		if (object == null) {
			return null;
		}
		Class<?> clazz = object.getClass();
		if (clazz.isEnum()) {
			String enumName = ((Enum<?>) object).name();
			try {
				return clazz.getField(enumName).getAnnotation(annotationClazz);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
				return null;
			}
		}
		return clazz.getAnnotation(annotationClazz);
	}

	/**
	 * 反射构造一个对象，如果设置了单例方法 getInstance,将直接使用单例而不是重新创建新对象
	 * 
	 * @param clazz
	 * @param <T>
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public static <T> T getOrCreateInstance(Class<T> clazz)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		try {
			Method method = clazz.getMethod("getInstance");
			Object instance = method.invoke(clazz);
			if (instance != null) {
				return clazz.cast(instance);
			}
		} catch (Exception ignored) {
		}
		if (clazz.isAnnotationPresent(NonSingleton.class)) {
			// 标注了非单例的类 不自动创建实例
			return null;
		}
		return clazz.getConstructor().newInstance();
	}

	/**
	 * 获取该类继承的指定父类/接口所指定的泛型类<br>
	 * 无法获取该类自身的泛型类
	 * 
	 * @param clazz
	 * @param rawClazz 包含泛型的父类/接口
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<?>[] getParameterizedTypeClazzes(Class<?> clazz, Class<?> rawClazz) {
		ParameterizedType genericType = null;
		Class<?> targetClazz = clazz;
		outer: while (rawClazz.isAssignableFrom(targetClazz)) {
			// 查找该类实现的接口
			Type[] genericInterfaces = targetClazz.getGenericInterfaces();
			if (genericInterfaces != null && genericInterfaces.length > 0) {
				for (Type genericInterface : genericInterfaces) {
					if (genericInterface instanceof ParameterizedType) {
						ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
						Type rawType = parameterizedType.getRawType();
						if (rawType.equals(rawClazz)) {
							genericType = parameterizedType;
							break outer;
						} else if (rawType instanceof Class) {
							Class<?> tmpRawType = (Class<?>) rawType;
							if (rawClazz.isAssignableFrom(tmpRawType)) {
								genericType = parameterizedType;
								break outer;
							}
						}
					}
				}
			}
			Type genericSuperclass = targetClazz.getGenericSuperclass();
			if (genericSuperclass != null) {
				if (genericSuperclass instanceof ParameterizedType) {
					ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
					Type rawType = parameterizedType.getRawType();
					if (rawType.equals(rawClazz)) {
						genericType = parameterizedType;
						break outer;
					} else if (rawType instanceof Class) {
						Class<?> tmpRawType = (Class<?>) rawType;
						if (rawClazz.isAssignableFrom(tmpRawType)) {
							genericType = parameterizedType;
							break outer;
						}
					}
				}
			}
			Class<?> targetSuperClazz = null;
			Class<?> superClass = targetClazz.getSuperclass();
			if (superClass != null && rawClazz.isAssignableFrom(superClass)) {
				targetSuperClazz = superClass;
			}
			if (targetSuperClazz == null) {
				for (Class<?> implInterface : targetClazz.getInterfaces()) {
					if (rawClazz.isAssignableFrom(implInterface)) {
						targetSuperClazz = implInterface;
						break;
					}
				}
			}
			if (targetSuperClazz == null) {
				return null;
			}
			targetClazz = targetSuperClazz;
		}
		if (genericType == null) {
			return null;
		}
		Type[] actualTypes = genericType.getActualTypeArguments();
		if (actualTypes == null || actualTypes.length <= 0) {
			return null;
		}
		Class<?>[] result = new Class<?>[actualTypes.length];
		for (int i = 0; i < actualTypes.length; i++) {
			Type actualType = actualTypes[i];
			if (actualType instanceof Class) {
				clazz = (Class<?>) actualType;
				result[i] = clazz;
			} else if (actualType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) actualType;
				clazz = (Class<?>) parameterizedType.getRawType();
				result[i] = clazz;
			} else {
				logger.warn("------> type class transform error,class:{};rawClass:{}", clazz.getSimpleName(),
						rawClazz.getSimpleName());
			}
		}
		return result;
	}

	/**
	 * 获取该变量的getter方法<br>
	 * 若get/is方法返回参数的类与变量不一致 无法识别<br>
	 * 
	 * @param clazz
	 * @param field
	 * @return
	 */
	public static Method getReadMethod(Class<?> clazz, Field field) {
		Method[] methods = clazz.getMethods();
		if (methods == null || methods.length <= 0) {
			return null;
		}
		String fieldName = field.getName();
		String upperFirstFieldName = fieldName.substring(0, 1).toUpperCase(ENGLISH) + fieldName.substring(1);

		Class<?> fieldType = field.getType();
		// 首字母大写 或保持原样(参数第2个字母为大写时 ide生成的getset不会使首字母大写)
		String methodName1 = null;
		String methodName2 = null;
		if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
			methodName1 = "is" + fieldName;
			methodName2 = "is" + upperFirstFieldName;
		} else {
			methodName1 = "get" + fieldName;
			methodName2 = "get" + upperFirstFieldName;
		}
		for (Method method : methods) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes != null && parameterTypes.length > 0) {
				continue;
			}
			Class<?> returnType = method.getReturnType();
			if (!returnType.equals(fieldType)) {
				continue;
			}
			String tmpMethodName = method.getName();
			if (StringUtils.equals(methodName1, tmpMethodName) || StringUtils.equals(methodName2, tmpMethodName)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * 获取该类的低于指定的类的次高级父类
	 * 
	 * @param clazz
	 * @param topestClazz
	 * @return
	 */
	public static <T> Class<? extends T> getTopClazzExtendsTopest(Class<? extends T> clazz, Class<T> topestClazz) {
		Class<? extends T> result = clazz;
		while (!result.equals(Object.class)) {
			Class<?> superClazz = result.getSuperclass();
			if (superClazz.equals(topestClazz)) {
				// 当前类的父类是指定的顶级类
				break;
			}
			if (!topestClazz.isAssignableFrom(superClazz)) {
				// 当前类的父类不是指定的类的子类/实现类
				break;
			}
			result = superClazz.asSubclass(topestClazz);
		}
		return result;
	}

	/**
	 * 获取该变量的setter方法<br>
	 * 若set方法参数的类与变量不一致 无法识别<br>
	 * 
	 * @param clazz
	 * @param field
	 * @return
	 */
	public static Method getWriteMethod(Class<?> clazz, Field field) {
		Method[] methods = clazz.getMethods();
		if (methods == null || methods.length <= 0) {
			return null;
		}
		// 首字母大写 或保持原样(参数第2个字母为大写时 ide生成的getset不会使首字母大写)
		String fieldName = field.getName();
		String upperFirstFieldName = fieldName.substring(0, 1).toUpperCase(ENGLISH) + fieldName.substring(1);
		String methodName1 = "set" + fieldName;
		String methodName2 = "set" + upperFirstFieldName;
		Class<?> fieldType = field.getType();
		for (Method method : methods) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes == null || parameterTypes.length != 1) {
				continue;
			}
			if (!parameterTypes[0].equals(fieldType)) {
				continue;
			}
			String tmpMethodName = method.getName();
			if (StringUtils.equals(methodName1, tmpMethodName) || StringUtils.equals(methodName2, tmpMethodName)) {
				return method;
			}
		}
		return null;
	}

	public static Method getMethod(Object obj, String methodName, Class<?>... args) throws NoSuchMethodException {
		if (obj == null) {
			throw new NullPointerException("object is null;");
		}
		Class<?> clazz = obj.getClass();
		return getMethod(clazz, methodName, args);
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) throws NoSuchMethodException {
		while (!clazz.equals(Object.class)) {
			try {
				Method method = clazz.getDeclaredMethod(methodName, args);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException e) {
				clazz = clazz.getSuperclass();
			}
		}
		Method method = clazz.getDeclaredMethod(methodName, args);
		method.setAccessible(true);
		return method;
	}

	/**
	 * 是否基础数据类型<br>
	 * 基础数据类型/枚举/基础数据类型的封装类/数字类/字符串
	 * 
	 * @param o
	 * @return
	 */
	public static boolean isBaseType(Object o) {
		Class<?> c = o.getClass();
		return isBaseType(c);
	}

	/**
	 * 是否基础数据类型<br>
	 * 基础数据类型/枚举/基础数据类型的封装类/数字类/字符串
	 * 
	 * @param o
	 * @return
	 */
	public static boolean isBaseType(Class<?> c) {
		if (c.isPrimitive()) {
			return true;
		}
		if (c.isEnum()) {
			return true;
		}
		if (c == Boolean.class || c == Byte.class || c == Short.class || c == Integer.class || c == Long.class //
				|| c == Float.class || c == Double.class || c == String.class || c == Character.class) {
			return true;
		}
		if (Number.class.isAssignableFrom(c)) {
			return true;
		}
		return false;
	}

	/**
	 * 通过某个接口或者抽象类的 Class 对象，创建出相应的对象 可以处理普通类，枚举（包括枚举直接实现接口，或者在枚举字段中实现接口）
	 * 
	 * @param implClasses 接口实现类 Class
	 * @param <T>         抽象类型
	 * @return objects
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> newImplClassesObjects(List<Class<? extends T>> implClasses)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		List<T> newObjects = new ArrayList<>(implClasses.size());
		for (Class<? extends T> implClass : implClasses) {
			if (implClass.isEnum()) { // 处理枚举直接实现接口
				Object[] objects = implClass.getEnumConstants();
				for (Object object : objects) {
					T obj = (T) object;
					newObjects.add(obj);
				}
			} else if (implClass.getSuperclass().isEnum()) {// 处理在枚举字段中实现接口的情况
				Class<?> superClass = implClass.getSuperclass();
				Object[] objects = superClass.getEnumConstants();
				for (Object object : objects) {
					if (object.getClass() == implClass) {
						T obj = (T) object;
						newObjects.add(obj);
						break;
					}
				}
			} else {
				T object = getOrCreateInstance(implClass);
				if (object != null) {
					newObjects.add(object);
				}
			}
		}
		return newObjects;
	}

	/**
	 * 查找game包下的所有符合条件的类
	 * 
	 * @param filter
	 * @return
	 */
	public static List<Class<?>> scanClassList(Predicate<Class<?>> filter) {
		return scanClassList(GAME_PACKAGE_NAME, filter);
	}

	/**
	 * 查找指定包名下的所有符合条件类<br>
	 * 含子包
	 * 
	 * @param packageName
	 * @param filter
	 * @return
	 */
	public static List<Class<?>> scanClassList(String packageName, @Nullable Predicate<Class<?>> filter) {
		List<Class<?>> classList = new ArrayList<>();
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> urls = classLoader.getResources(packageName.replaceAll("\\.", "/"));
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (url == null) {
					continue;
				}
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					String packagePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
					File packageFile = new File(packagePath);
					if (!packageFile.exists() || !packageFile.isDirectory()) {
						continue;
					}
					File[] childFiles = packageFile.listFiles();
					for (File childFile : childFiles) {
						String childFileName = childFile.getName();
						if (childFile.isDirectory()) {
							// 文件夹 递归查找子包
							String subPackageName = packageName + "." + childFileName;
							List<Class<?>> subClassList = scanClassList(subPackageName, filter);
							classList.addAll(subClassList);
						} else if (childFileName.endsWith(".class")) {
							// class文件
							String clazzName = packageName + "."
									+ childFileName.substring(0, childFileName.length() - 6);
							Class<?> clazz = Class.forName(clazzName);
							if (filter.test(clazz)) {
								classList.add(clazz);
							}
						}
					}

				} else if ("jar".equals(protocol)) {
					JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
					JarFile jarFile = jarURLConnection.getJarFile();
					Enumeration<JarEntry> jarEntries = jarFile.entries();
					while (jarEntries.hasMoreElements()) {
						JarEntry jarEntry = jarEntries.nextElement();
						String name = jarEntry.getName();
						if (jarEntry.isDirectory() || !name.endsWith(".class")) {
							// 文件夹 或者不是class文件
							continue;
						}
						String clazzName = name.substring(0, name.length() - 6).replaceAll("/", ".");
						if (!clazzName.startsWith(packageName)) {
							continue;
						}
						try {
							Class<?> clazz = Class.forName(clazzName);
							if (filter == null || filter.test(clazz)) {
								classList.add(clazz);
							}
						} catch (Exception e) {
							logger.error("clazz[{}] error", clazzName, e);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("scanClassList[{}] error.", packageName, e);
		}
		return classList;
	}

	public static <T> List<T> scanImplAndNewInstances(String packageName, Class<T> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		List<Class<?>> implClazzes = scanClassList(packageName, tmpClazz -> {
			if (!clazz.isAssignableFrom(tmpClazz)) {
				return false;
			}
			if (tmpClazz.isInterface()) {
				return false;
			}
			return !Modifier.isAbstract(tmpClazz.getModifiers());
		});
		List<T> instances = new ArrayList<>(implClazzes.size());
		if (implClazzes.isEmpty()) {
			return Collections.emptyList();
		}
		for (Class<?> implClazz : implClazzes) {
			if (implClazz.isEnum()) { // 处理枚举直接实现接口
				Object[] enums = implClazz.getEnumConstants();
				for (Object enumInstance : enums) {
					T obj = clazz.cast(enumInstance);
					instances.add(obj);
				}
			} else if (implClazz.getSuperclass().isEnum()) {// 处理在枚举字段中实现接口的情况
				Class<?> superClass = implClazz.getSuperclass();
				Object[] enums = superClass.getEnumConstants();
				for (Object enumInstance : enums) {
					if (enumInstance.getClass() == implClazz) {
						T obj = clazz.cast(enumInstance);
						instances.add(obj);
						break;
					}
				}
			} else {
				Object instance = getOrCreateInstance(implClazz);
				if (instance != null) {
					T object = clazz.cast(instance);
					instances.add(object);
				}
			}
		}
		return instances;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<Class<? extends T>> scanImplClasses(String packageName, final Class<T> clazz) {
		return scanClassList(packageName, tmpClazz -> {
			if (!clazz.isAssignableFrom(tmpClazz)) {
				return false;
			}
			if (tmpClazz.isInterface()) {
				return false;
			}
			return !Modifier.isAbstract(tmpClazz.getModifiers());
		}).stream().map(tempClazz -> (Class<? extends T>) tempClazz).collect(Collectors.toList());
	}

}
