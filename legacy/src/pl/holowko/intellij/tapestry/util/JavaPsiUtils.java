package pl.holowko.intellij.tapestry.util;

import com.intellij.psi.PsiClass;

public class JavaPsiUtils {

    public static boolean isInstanceOf(PsiClass instance, String interfaceExtendsClassName) {
        String qualifiedName = instance.getQualifiedName();
        if (qualifiedName.equals(interfaceExtendsClassName)) {
            return true;
        }

        PsiClass[] interfaces = instance.getInterfaces();

        for (PsiClass anInterface : interfaces) {
            if (isInstanceOf(anInterface, interfaceExtendsClassName)) {
                return true;
            }
        }

        PsiClass[] supers = instance.getSupers();
        for (PsiClass aSuper : supers) {
            if (isInstanceOf(aSuper, interfaceExtendsClassName)) {
                return true;
            }
        }

        return false;
    }

}