import java.util.*;

public class Exercise5_2 {
    public static void main(String[] args) {
        Dog spot = new Dog("spot", "Ruff!");
        Dog scruffy = new Dog("Scruffy", "Wurf!");

        System.out.println("A dog named " + spot.name + " says " + spot.says);
        System.out.println("A dog named " + scruffy.name + " says " + scruffy.says);

        Dog[] index = new Dog[10];

        for (Dog d : index) {
            d = spot;
            System.out.println("d == spot? " + (d == spot));
            System.out.println("d.equals(spot)? " + d.equals(spot));
        }
    }
}

class Dog {
    public String name;
    public String says;

    public Dog(String s1, String s2) {
        this.name = s1;
        this.says = s2;
    }
}