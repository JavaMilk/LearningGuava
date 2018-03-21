package Collections.ImmutableCollections;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @author jianhonghu
 * @date 2018/3/20
 */
public class Example {

    public static final ImmutableSet<String> COLOR_NAMES = ImmutableSet.of(
            "red",
            "orange",
            "yellow",
            "green",
            "blue",
            "purple"
    );

    class Bar {

    }

    class Foo {
        final ImmutableSet<Bar> bars;
        Foo(Set<Bar> bars){
            this.bars = ImmutableSet.copyOf(bars);
        }
    }
}
