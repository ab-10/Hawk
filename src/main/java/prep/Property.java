package prep;

public class Property {
    private final String value, role;

    public Property(String value, String role){
        this.value = value;
        this.role = role;
    }

    public String toString(){
        return this.value;
    }

    public String getRole(){
        return this.role;
    }
}
