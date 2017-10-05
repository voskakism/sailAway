public class Application
{
	public static void main(String[] args)
	{
		System.out.println(GlobalClass.intVar);
		GlobalClass.intVar = 2;
		System.out.println(GlobalClass.intVar);
	}
}