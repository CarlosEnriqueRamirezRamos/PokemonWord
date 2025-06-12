package com.pokemon.PokemonWord.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/PokemonWord")
public class PokemonController {

    private static final String POKEAPI_URL = "https://pokeapi.co/api/v2/";
    private final RestTemplate restTemplate;

    public PokemonController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("darkMode", true);
        model.addAttribute("pageTitle", "Mundo Pokemon");

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(POKEAPI_URL + "pokemon?limit=53", Map.class);
            List<Map<String, String>> pokemons = (List<Map<String, String>>) response.getBody().get("results");

            if (pokemons == null || pokemons.isEmpty()) {
                model.addAttribute("error", "La lista de Pokémon está vacía");
            } else {
                model.addAttribute("pokemons", pokemons);
                System.out.println("Pokémones cargados: " + pokemons.size()); // Debug
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al consumir la API: " + e.getMessage());
        }

        return "Pokemones";
    }

    @GetMapping("/Details/{id}")
    public String getPokemonDetails(@PathVariable int id, Model model) {

        try {
            Map<String, Object> pokemonData = getApiData("pokemon/" + id);
            if (pokemonData == null) {
                model.addAttribute("error", "No se encontró el Pokémon");
                return "Details";
            }
            
            //Obtencion del sonido Pokemon
            String name = (String) pokemonData.get("name");
            String cryUrl = "https://play.pokemonshowdown.com/audio/cries/" + name + ".mp3";
            model.addAttribute("cryUrl", cryUrl);


            model.addAttribute("pokemon", pokemonData);

            Map<String, Object> speciesData = getApiData(((Map<String, String>) pokemonData.get("species")).get("url"));
            model.addAttribute("species", speciesData);

            if (speciesData != null && speciesData.containsKey("evolution_chain")) {
                Map<String, String> evolutionChain = (Map<String, String>) speciesData.get("evolution_chain");
                Map<String, Object> evolutionData = getApiData(evolutionChain.get("url"));
                model.addAttribute("evolution", evolutionData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al obtener el Pokémon: " + e.getMessage());
        }

        return "Details";
    }

    private Map<String, Object> getApiData(String endpointOrUrl) {
        // Si la cadena empieza con "http", es una URL completa, sino un endpoint relativo
        String url = endpointOrUrl.startsWith("http") ? endpointOrUrl : POKEAPI_URL + endpointOrUrl;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }
}
