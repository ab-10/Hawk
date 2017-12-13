import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import prep.Definition;
import prep.Graph;
import prep.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertyTests {
    private static Property someProperty;
    private static Property anotherProperty;


    @BeforeAll
    public static void initialise(){
        someProperty = new Property("for solo voice", "has_diff_qual", "song");
        anotherProperty = new Property("baked", "has_diff_event", "beans");
    }

    @Test
    public void canFindRoles(){
        assertEquals("has_diff_qual", someProperty.getRole()
                , "Fails to identify the correct role for a property");
        assertEquals("has_diff_event", anotherProperty.getRole()
                , "Fails to identify the correct role for a property");
    }

    @Test
    public void canFindValue(){
        assertEquals("for solo voice", someProperty.getValue()
                , "Fails to identify the correct value of a property");
        assertEquals("baked", anotherProperty.getValue()
                , "Fails to identify the correct value of a property");
    }

    @Test
    public void canFindSubject(){
        assertEquals("song", someProperty.getSubject()
                    , "Fails to identify the correct subject of a property");
        assertEquals("beans", anotherProperty.getSubject()
                    , "Fails to identify the correct subject of a property");
    }

    @Test
    public void canBeRepresentedAsString(){
        assertEquals("Property(\"for solo voice\", \"has_diff_qual\", \"song\")", someProperty.toString()
                    , "Fails to represent a property as a String");
        assertEquals("Property(\"baked\", \"has_diff_event\", \"beans\")", anotherProperty.toString()
                    , "Fails to represent a property as a String");
    }
}
