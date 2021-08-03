package me.vixen.chopperbot.database;

public class Command {
	private final String name;
	private final String response;
	private final boolean staffOnly;

	protected Command(String name, String response, boolean staffOnly) {
		this.name = name;
		this.response = response;
		this.staffOnly = staffOnly;
	}

	public String getName() {
		return name;
	}

	public String getResponse() {
		return response;
	}

	public boolean isStaffOnly() {
		return staffOnly;
	}

	public static class Builder {
		private String name;
		private String response;
		private boolean staffOnly;

		public Builder setName(String name) {
			this.name = name;
			return this;
		}
		public Builder setResponse(String response) {
			this.response = response.replaceAll("<n>", "\n");
			return this;
		}
		public Builder setStaffOnly(boolean staffOnly) {
			this.staffOnly = staffOnly;
			return this;
		}

		public Command build() {
			return new Command(name,response,staffOnly);
		}
	}
}