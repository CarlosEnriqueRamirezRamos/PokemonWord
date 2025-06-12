// Archivo: src/main/java/com/pokemon/PokemonWord/ML/Pokemon.java
package com.pokemon.PokemonWord.ML;

import lombok.Data;
import java.util.List;

@Data
public class Pokemon {
    private Integer id;
    private String name;
    private Integer height;
    private Integer weight;
    private List<PokemonType> types;
    private Sprites sprites;
    
    @Data
    public static class PokemonType {
        private Type type;
        
        @Data
        public static class Type {
            private String name;
        }
    }
    
    @Data
    public static class Sprites {
        private String front_default;
    }
}