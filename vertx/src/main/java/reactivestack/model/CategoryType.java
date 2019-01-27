package reactivestack.model;

import lombok.Getter;

@Getter
public enum CategoryType {
    //https://unicode.org/emoji/charts/full-emoji-list.html
    SMILEYS_AND_EMOTION("Smileys & Emotion"),
    PEOPLE_AND_BODY("People & Body"),
    COMPONENT("Component"),
    ANIMALS_AND_NATURE("Animals & Nature"),
    FOOD_AND_DRINK("Food & Drink"),
    TRAVEL_AND_PLACES("Travel & Places"),
    ACTIVITIES("Activities"),
    OBJECTS("Objects"),
    SYMBOLS("Symbols"),
    FLAGS("Flags");

    private final String longName;

    CategoryType(final String longName) {
        this.longName = longName;
    }

}
