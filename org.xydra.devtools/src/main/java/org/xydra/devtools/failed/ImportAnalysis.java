/***
 * ASM examples: examples showing how ASM can be used Copyright (c) 2000-2007
 * INRIA, France Telecom All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.xydra.devtools.failed;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.scanners.TypesScanner;
import org.reflections.util.ClasspathHelper;


public class ImportAnalysis {
    
    public static void main(String[] args) throws IOException, AnalyzerException {
        
        Reflections reflections = new Reflections(
        
        ClasspathHelper.forPackage("com.calpano"), ClasspathHelper.forPackage("org.xydra"),
                new SubTypesScanner(false), new TypesScanner(), new TypeElementsScanner());
        
        Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);
        
        System.out.println(allClasses.size() + " classes");
        for(Class<?> c : allClasses) {
            System.out.println(c.getCanonicalName());
            
            String className = c.getCanonicalName(); // e.g. "foo.Bar"
            
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            // map className to a resource name; e.g. "/foo/Bar.class"
            // or "org/ontoware/rdf2go/testdata/foaf.xml"
            String resourceName = className.replace(".", "/") + ".class";
            System.out.println("Loading " + resourceName);
            
            InputStream in = cl.getResourceAsStream(resourceName);
            
            DependencyTracker.process(resourceName, in);
        }
        
        DependencyTracker.buildDiagram();
    }
    
    /**
     * DependencyVisitor
     * 
     * @author Eugene Kuleshov
     */
    public static class DependencyVisitor implements AnnotationVisitor, SignatureVisitor,
            ClassVisitor, FieldVisitor, MethodVisitor {
        Set<String> packages = new HashSet<String>();
        
        Map<String,Map<String,Integer>> groups = new HashMap<String,Map<String,Integer>>();
        
        Map<String,Integer> current;
        
        public Map<String,Map<String,Integer>> getGlobals() {
            return this.groups;
        }
        
        public Set<String> getPackages() {
            return this.packages;
        }
        
        // ClassVisitor
        
        public void visit(final int version, final int access, final String name,
                final String signature, final String superName, final String[] interfaces) {
            String p = getGroupKey(name);
            this.current = this.groups.get(p);
            if(this.current == null) {
                this.current = new HashMap<String,Integer>();
                this.groups.put(p, this.current);
            }
            
            if(signature == null) {
                addInternalName(superName);
                addInternalNames(interfaces);
            } else {
                addSignature(signature);
            }
        }
        
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            addDesc(desc);
            return this;
        }
        
        public void visitAttribute(final Attribute attr) {
        }
        
        public FieldVisitor visitField(final int access, final String name, final String desc,
                final String signature, final Object value) {
            if(signature == null) {
                addDesc(desc);
            } else {
                addTypeSignature(signature);
            }
            if(value instanceof Type) {
                addType((Type)value);
            }
            return this;
        }
        
        public MethodVisitor visitMethod(final int access, final String name, final String desc,
                final String signature, final String[] exceptions) {
            if(signature == null) {
                addMethodDesc(desc);
            } else {
                addSignature(signature);
            }
            addInternalNames(exceptions);
            return this;
        }
        
        public void visitSource(final String source, final String debug) {
        }
        
        public void visitInnerClass(final String name, final String outerName,
                final String innerName, final int access) {
            // addName( outerName);
            // addName( innerName);
        }
        
        public void visitOuterClass(final String owner, final String name, final String desc) {
            // addName(owner);
            // addMethodDesc(desc);
        }
        
        // MethodVisitor
        
        public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc,
                final boolean visible) {
            addDesc(desc);
            return this;
        }
        
        public void visitTypeInsn(final int opcode, final String type) {
            addType(Type.getObjectType(type));
        }
        
        public void visitFieldInsn(final int opcode, final String owner, final String name,
                final String desc) {
            addInternalName(owner);
            addDesc(desc);
        }
        
        public void visitMethodInsn(final int opcode, final String owner, final String name,
                final String desc) {
            addInternalName(owner);
            addMethodDesc(desc);
        }
        
        public void visitLdcInsn(final Object cst) {
            if(cst instanceof Type) {
                addType((Type)cst);
            }
        }
        
        public void visitMultiANewArrayInsn(final String desc, final int dims) {
            addDesc(desc);
        }
        
        public void visitLocalVariable(final String name, final String desc,
                final String signature, final Label start, final Label end, final int index) {
            addTypeSignature(signature);
        }
        
        public AnnotationVisitor visitAnnotationDefault() {
            return this;
        }
        
        public void visitCode() {
        }
        
        public void visitFrame(final int type, final int nLocal, final Object[] local,
                final int nStack, final Object[] stack) {
        }
        
        public void visitInsn(final int opcode) {
        }
        
        public void visitIntInsn(final int opcode, final int operand) {
        }
        
        public void visitVarInsn(final int opcode, final int var) {
        }
        
        public void visitJumpInsn(final int opcode, final Label label) {
        }
        
        public void visitLabel(final Label label) {
        }
        
        public void visitIincInsn(final int var, final int increment) {
        }
        
        public void visitTableSwitchInsn(final int min, final int max, final Label dflt,
                final Label[] labels) {
        }
        
        public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        }
        
        public void visitTryCatchBlock(final Label start, final Label end, final Label handler,
                final String type) {
            addInternalName(type);
        }
        
        public void visitLineNumber(final int line, final Label start) {
        }
        
        public void visitMaxs(final int maxStack, final int maxLocals) {
        }
        
        // AnnotationVisitor
        
        public void visit(final String name, final Object value) {
            if(value instanceof Type) {
                addType((Type)value);
            }
        }
        
        public void visitEnum(final String name, final String desc, final String value) {
            addDesc(desc);
        }
        
        public AnnotationVisitor visitAnnotation(final String name, final String desc) {
            addDesc(desc);
            return this;
        }
        
        public AnnotationVisitor visitArray(final String name) {
            return this;
        }
        
        // SignatureVisitor
        
        String signatureClassName;
        
        public void visitFormalTypeParameter(final String name) {
        }
        
        public SignatureVisitor visitClassBound() {
            return this;
        }
        
        public SignatureVisitor visitInterfaceBound() {
            return this;
        }
        
        public SignatureVisitor visitSuperclass() {
            return this;
        }
        
        public SignatureVisitor visitInterface() {
            return this;
        }
        
        public SignatureVisitor visitParameterType() {
            return this;
        }
        
        public SignatureVisitor visitReturnType() {
            return this;
        }
        
        public SignatureVisitor visitExceptionType() {
            return this;
        }
        
        public void visitBaseType(final char descriptor) {
        }
        
        public void visitTypeVariable(final String name) {
        }
        
        public SignatureVisitor visitArrayType() {
            return this;
        }
        
        public void visitClassType(final String name) {
            this.signatureClassName = name;
            addInternalName(name);
        }
        
        public void visitInnerClassType(final String name) {
            this.signatureClassName = this.signatureClassName + "$" + name;
            addInternalName(this.signatureClassName);
        }
        
        public void visitTypeArgument() {
        }
        
        public SignatureVisitor visitTypeArgument(final char wildcard) {
            return this;
        }
        
        // common
        
        public void visitEnd() {
        }
        
        // ---------------------------------------------
        
        private String getGroupKey(String name) {
            String name2 = name;
            int n = name2.lastIndexOf('/');
            if(n > -1) {
                name2 = name2.substring(0, n);
            }
            this.packages.add(name2);
            return name2;
        }
        
        private void addName(final String name) {
            if(name == null) {
                return;
            }
            String p = getGroupKey(name);
            if(this.current.containsKey(p)) {
                this.current.put(p, this.current.get(p) + 1);
            } else {
                this.current.put(p, 1);
            }
        }
        
        private void addInternalName(final String name) {
            addType(Type.getObjectType(name));
        }
        
        private void addInternalNames(final String[] names) {
            for(int i = 0; names != null && i < names.length; i++) {
                addInternalName(names[i]);
            }
        }
        
        private void addDesc(final String desc) {
            addType(Type.getType(desc));
        }
        
        private void addMethodDesc(final String desc) {
            addType(Type.getReturnType(desc));
            Type[] types = Type.getArgumentTypes(desc);
            for(int i = 0; i < types.length; i++) {
                addType(types[i]);
            }
        }
        
        private void addType(final Type t) {
            switch(t.getSort()) {
            case Type.ARRAY:
                addType(t.getElementType());
                break;
            case Type.OBJECT:
                addName(t.getInternalName());
                break;
            }
        }
        
        private void addSignature(final String signature) {
            if(signature != null) {
                new SignatureReader(signature).accept(this);
            }
        }
        
        private void addTypeSignature(final String signature) {
            if(signature != null) {
                new SignatureReader(signature).acceptType(this);
            }
        }
    }
}
