package me.vixen.chopperbot.tools;


public enum Errors {
	JDANULLRETURN("API failed to return necessary information"),
	DBNULLRETURN("Database failed to return necessary information"),
	CONFIG1("Database failed to set config"),
	COMMAND1("Database failed to INSERT command"),
	LOTTOADD("Database faild to add a lotto bet"),
	PROFILE1("IO failed to draw image");

	private final String errorText;

	Errors(String errorText) {
		this.errorText = errorText;
	}

	public String get() {
		return this.errorText;
	}
}
