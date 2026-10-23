package plus.tacks;

public class NullTask implements Task{
    @Override
    public boolean run(String value) {
        System.out.println("Unknown command!");
        return false;
    }

    @Override
    public String getName() {
        return "error";
    }
}
