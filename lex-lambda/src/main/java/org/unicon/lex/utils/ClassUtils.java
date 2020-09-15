package org.unicon.lex.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.services.external.AthenaService;
import org.unicon.lex.services.external.AthenaServiceImpl;
import org.unicon.lex.services.external.CanvasService;
import org.unicon.lex.services.external.CanvasServiceImpl;
import org.unicon.lex.services.external.KinesisService;
import org.unicon.lex.services.external.RDSService;
import org.unicon.lex.services.external.RDSServiceImpl;
import org.unicon.lex.services.external.S3Service;
import org.unicon.lex.services.external.S3ServiceImpl;
import org.unicon.lex.services.intent.LexService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ClassUtils {
    final Logger log = LogManager.getLogger(getClass());

    public Map<String, LexService> buildLexServices(Properties properties) {
        AthenaService athenaService = new AthenaServiceImpl(properties);
        RDSService rdsService = new RDSServiceImpl(properties);
        S3Service s3Service = new S3ServiceImpl(properties);
        KinesisService kinesisService = new KinesisService(properties);
        CanvasService canvasService = new CanvasServiceImpl(properties);

        Map<String, LexService> results = new HashMap<>();
        try {
            List<Class<?>> clazzes = findClassesImpmenenting(LexService.class, "org.unicon.lex");
            for (Class clazz : clazzes) {
                log.error("class [{}] implements LexUtils", clazz.getName());

                Class<LexService> serviceClazz = (Class<LexService>) Class.forName(clazz.getName());

                // I want to make instantiating LexService classes more flexible.  I 
                // don't want to require all LexService classes to have an AthenaService,
                // just the ones that need it.  Not sure if this should be a constructor param
                // or a setter.
//                Class<?> parameterTypes = null;
//                serviceClazz.getDeclaredConstructor(parameterTypes, parameterTypes, parameterTypes);
//                Constructor<?> constructor = serviceClazz.getConstructor(String.class, Integer.class);

//                Constructor<?>[] constructors = serviceClazz.getConstructors();
//                for (Constructor<?> constructor : constructors) {
//                    Class<?>[] paramTypes = constructor.getParameterTypes();
//                    for (Class<?> paramType : paramTypes) {
//                        if (paramType.equals(obj))
//                    }
//                }

                LexService service = serviceClazz.newInstance();
                service.setAthenaService(athenaService);
                service.setRDSService(rdsService);
                service.setS3Service(s3Service);
                service.setKinesisService(kinesisService);
                service.setCanvasService(canvasService);
                results.put(service.getName(), service);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return results;
    }

    public List<Class<?>> findClassesImpmenenting(final Class<?> interfaceClass, String fromPackage) {

        if (interfaceClass == null) {
            log.error("Unknown subclass.");
            return null;
        }

        if (fromPackage == null) {
            log.error("Unknown package.");
            return null;
        }

        final List<Class<?>> rVal = new ArrayList<Class<?>>();
        try {
            final Class<?>[] targets = getAllClassesFromPackage(fromPackage);
            if (targets != null) {
                for (Class<?> aTarget : targets) {
                    if (aTarget == null) {
                        continue;
                    } else if (aTarget.equals(interfaceClass)) {
                        continue;
                    } else if (!interfaceClass.isAssignableFrom(aTarget)) {
                        continue;
                    } else if (Modifier.isAbstract(aTarget.getModifiers())) {
                        continue;
                    } else {
                        rVal.add(aTarget);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Error reading package name.");
            log.error(e);
        } catch (IOException e) {
            log.error("Error reading classes in package.");
            log.error(e);
        }

        return rVal;
    }

    public static Class[] getAllClassesFromPackage(final String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(
                        Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
