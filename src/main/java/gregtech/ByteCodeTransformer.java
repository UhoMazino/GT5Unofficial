package gregtech;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class ByteCodeTransformer implements IClassTransformer {
    public MethodNode getByName(ClassNode cn, String name) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(name)) {
                return mn;
            }
        }
        return null;
    }

    public MethodNode getByNameAndSign(ClassNode cn, String name, String desc) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(name) && mn.desc.equals(desc)) {
                return mn;
            }
        }
        return null;
    }

    public byte[] preformTransform(byte[] bytes, boolean obf) {
        ClassNode cn = new ClassNode();
        ClassReader cr = new ClassReader(bytes);
        cr.accept(cn, 0);
        System.out.println("Class found");
        MethodNode s;
        if (obf) {
            s = getByNameAndSign(cn, "a", "(IIILaor;)V");
        } else {
            s = getByName(cn, "setTileEntity");
        }
        if (s != null) {
            System.out.println("Method found, patching");
            AbstractInsnNode place = s.instructions.toArray()[114];
            s.instructions.insert(place, new JumpInsnNode(IFNE, (LabelNode) s.instructions.toArray()[126]));
            s.instructions.insert(place, new TypeInsnNode(INSTANCEOF, "gregtech/common/blocks/GT_TileEntity_Ores"));
            s.instructions.insert(place, new VarInsnNode(ALOAD, 4));
            System.out.println("Patch successful");
        }
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);

        return cw.toByteArray();
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (name.equals("net.minecraft.world.World")) {
            return preformTransform(bytes, false);
        } else if (name.equals("ahb")) {
            return preformTransform(bytes, true);
        }
        return bytes;
    }
}