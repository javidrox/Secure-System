import java.io.*;
import java.util.*;

class SecureSystem
{

	public static void printState(SysObject lobj, SysObject hobj, Subject lyle, Subject hal)
	{
		System.out.println("The current state is:");
		System.out.println("	lobj has value: " + lobj.value);
		System.out.println("	hobj has value: " + hobj.value);
		System.out.println("	lyle has recently read: " + lyle.temp);
		System.out.println("	hal has recently read: " + hal.temp);
	}

	public static void main(String[] args) throws IOException
	{
		int low = SecurityLevel.LOW;
		int high = SecurityLevel.HIGH;

		ReferenceMonitor rm = new ReferenceMonitor();

		Subject lyle = rm.createSubject("lyle", low);
		Subject hal = rm.createSubject("hal", high);

		SysObject lobj = rm.createObject("Lobj", low);
		SysObject hobj = rm.createObject("Hobj", high);

		Scanner scan = new Scanner(new FileReader("instructionList.txt"));
		String line;
		while(scan.hasNext())
		{
			line = scan.nextLine();
			InstructionObject nextinstr = new InstructionObject(line);
			rm.executeInstr(nextinstr);
			printState(lobj, hobj, lyle, hal);
		}
	}
}

class ReferenceMonitor
{
	static ObjectManager objmgr = new ObjectManager();
	static HashMap<String, Integer> subjLevelMap = new HashMap<String, Integer>();
	static HashMap<String, Subject> subjNameMap = new HashMap<String, Subject>();

	public static createSubject(String name, int level)
	{
		Subject currentSubject = new Subject(name);
		subjLevelMap.put(name, level);
		subjNameMap.put(name, currentSubject);
	}

	public static createObject(String name, int level)
	{
		SysObject currentObj = new SysObject(name);
		objmgr.storeObject(name, level, currentObj);
	}

	public void executeInstr(InstructionObject instruction)
	{
		String currentSubj = instruction.subj;
		String currentObj = instruction.obj;
		if(instruction.instr.equals("BADINSTRUCTION"))
		{
			System.out.println("Bad instruction");
		}
		else if(subjNameMap.get(subj) != null  &&  objNameMap.get(obj) != null)
		{
			if(instruction.instr.equals("READ"))
			{
				executeRead(instruction.subj, instruction.obj);
			}
			else if(instruction.instr.equals("WRITE"))
			{
				executeWrite(instruction.obj, instruction.writeval);
			}
			else
			{
				System.out.println("Bad Instruction");
			}
		}
		else
		{
			System.out.println("Bad Instruction");
		}
	}

	public String executeRead(String subj, String obj)
	{
		if(SecurityLevel.dominates(subjLevelMap.get(subj), objmgr.objLevelMap.get(obj)))
		{
			objmgr.objRead(subjNameMap.get(subj), objmgr.objNameMap(obj));
		}
		else
		{
			// read not allowed
			subjNameMap.get(subj).temp = 0;
		}
		System.out.println(subj + " reads " + obj);
	}

	public void executeWrite(String subj, String obj, int val)
	{
		if(SecurityLevel.dominates(objmgr.objLevelMap.get(obj), subjLevelMap.get(subj)))
		{
			objmgr.objWrite(objmgr.objNameMap(obj), val);
		}
		else
		{
			// write not allowed
		}
		System.out.println(subj + " writes value " + val + " to " + obj);
	}

	static class ObjectManager
	{
		public static HashMap<String, Integer> objLevelMap = new HashMap<String, Integer>();
		public static HashMap<String, SysObject> objNameMap = new HashMap<String, SysObject>();

		public void storeObject(String name, int level, SysObject obj)
		{
			objLevelMap.put(name, level);
			objNameMap.put(name, obj);
		}

		public void objRead(Subject sub, SysObject obj)
		{
			sub.temp = obj.value;
		}

		public void objWrite(SysObject obj, int val)
		{
			obj.value = val;
		}
	}

}

class InstructionObject
{
	public String instr;
	public String subj;
	public String obj;
	public int writeval;

	public InstructionObject(String line)
	{
		String[] words = line.split(" ");
		if(words.length == 3)
		{
			if(words[0].toUpperCase().equals("READ"))
			{
				instr = "READ";
				subj = words[1].toLowerCase();
				obj = words[2].toLowerCase();
			}
			else
			{
				instr = "BADINSTRUCTION";
			}
		}
		else if(words.length == 4)
		{
			if(words[0].toUpperCase().equals("WRITE"))
			{
				instr = "WRITE";
				subj = words[1].toLowerCase();
				obj = words[2].toLowerCase();
				writeval = Integer.parseInt(words[3]);
			}
			else
			{
				instr = "BADINSTRUCTION";
			}
		}
		else
		{
			instr = "BADINSTRUCTION";
		}
	}

}

static class SecurityLevel
{
	public static final int LOW = 0;
	public static final int HIGH = 1;
	
	public static boolean dominates(int subjectLevel, int objectLevel)
	{
		if(subjectLevel >= objectLevel)
		{
			return true;
		}
		return false;
	}
}

class Subject
{
	public String name;
	public int temp;

	public Subject(String newname)
	{
		name = newname;
		temp = 0;
	}
}

class SysObject
{
	public String name;
	public int value;

	public SysObject(String newname)
	{
		name = newname;
		value = 0;
	}
}