package squeek.veganoption.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class ClassTransformer implements IClassTransformer
{

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (transformedName.equals("net.minecraft.block.BlockDynamicLiquid"))
		{
			boolean isObfuscated = name != transformedName;

			ClassNode classNode = readClassFromBytes(bytes);

			MethodNode method = findMethodNodeOfClass(classNode, isObfuscated ? "h" : "func_149813_h", isObfuscated ? "(Lahb;IIII)V" : "(Lnet/minecraft/world/World;IIII)V");

			/*
			if (Hooks.onFlowIntoBlock(null, 0, 0, 0, 0))
				return null;
			*/
			InsnList toInject = new InsnList();
			LabelNode ifNotCanceled = new LabelNode();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
			toInject.add(new VarInsnNode(Opcodes.ILOAD, 2));
			toInject.add(new VarInsnNode(Opcodes.ILOAD, 3));
			toInject.add(new VarInsnNode(Opcodes.ILOAD, 4));
			toInject.add(new VarInsnNode(Opcodes.ILOAD, 5));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class), "onFlowIntoBlock", "(Lnet/minecraft/world/World;IIII)Z"));
			toInject.add(new JumpInsnNode(Opcodes.IFEQ, ifNotCanceled));
			toInject.add(new InsnNode(Opcodes.RETURN));
			toInject.add(ifNotCanceled);

			method.instructions.insertBefore(findFirstInstruction(method), toInject);

			return writeClassToBytes(classNode);
		}
		return bytes;
	}

	private ClassNode readClassFromBytes(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		return classNode;
	}

	private byte[] writeClassToBytes(ClassNode classNode)
	{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private MethodNode findMethodNodeOfClass(ClassNode classNode, String methodName, String methodDesc)
	{
		for (MethodNode method : classNode.methods)
		{
			if (method.name.equals(methodName) && method.desc.equals(methodDesc))
			{
				return method;
			}
		}
		return null;
	}

	public AbstractInsnNode getOrFindInstruction(AbstractInsnNode firstInsnToCheck)
	{
		return getOrFindInstruction(firstInsnToCheck, false);
	}

	public AbstractInsnNode getOrFindInstruction(AbstractInsnNode firstInsnToCheck, boolean reverseDirection)
	{
		for (AbstractInsnNode instruction = firstInsnToCheck; instruction != null; instruction = reverseDirection ? instruction.getPrevious() : instruction.getNext())
		{
			if (instruction.getType() != AbstractInsnNode.LABEL && instruction.getType() != AbstractInsnNode.LINE)
				return instruction;
		}
		return null;
	}

	public AbstractInsnNode findFirstInstruction(MethodNode method)
	{
		return getOrFindInstruction(method.instructions.getFirst());
	}
}