package io.github.linagora.linid.im.plugin.config;

import java.net.URL;
import java.net.URLClassLoader;

public class ChildFirstClassLoader extends URLClassLoader {

  public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  @Override
  protected synchronized Class<?> loadClass(String name, boolean resolve)
    throws ClassNotFoundException {

    Class<?> loadedClass = findLoadedClass(name);

    if (loadedClass == null) {
      try {
        loadedClass = findClass(name);
      } catch (ClassNotFoundException e) {
        loadedClass = super.loadClass(name, false);
      }
    }

    if (resolve) {
      resolveClass(loadedClass);
    }

    return loadedClass;
  }
}