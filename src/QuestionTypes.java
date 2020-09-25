public enum QuestionTypes {
    PS("Případová studie"),
    VSO("Více správných odpovědí"),
    JSO("Jedna správná odpověď"),
    NA("Nedefinovane");


    private String value;

    QuestionTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
