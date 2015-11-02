package luminis.whisky.resources;


import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class DemoResourceTest {
    private final String template = "Hello, %s";

    private DemoResource underTest;

    @Before
    public void setup() {
        underTest = new DemoResource(template);
    }

    @Test
    public void should_say_hello() {
        String name = "Dodo";
        Optional<String> optional = Optional.of(name);

        assertEquals("Hello, Dodo", underTest.sayHello(optional).get("hello"));
    }
}
