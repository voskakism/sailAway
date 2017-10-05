import java.util.LinkedList;
import java.util.ListIterator;

public class App
{
	public static void main(String[] args)
	{
		LinkedList<Integer> ll = new LinkedList<Integer>();
		ll.add(new Integer(1));
		ll.add(new Integer(2));
		ll.add(new Integer(3));
		ll.add(new Integer(4));
		
		printLL(ll);
		System.out.println(ll.peek());
		System.out.println(ll.peekFirst());
		System.out.println(ll.peekLast());
		printLL(ll);
		
		/*also try:
		peekFirst
		peekLast
		remove
		poll
		addFirst*/
	}
	
	public static void printLL(LinkedList<Integer> ll)
	{
		System.out.println(ll);
	}
}