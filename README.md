![Logo del progetto](Photo/QueryGUI.png)

# Music Streaming Platform Database Project for distributed systems

This repository contains the files for a database project that models a music streaming platform. The goal of this project is to design and implement a relational database schema that can effectively store and manage data related to users, artists, songs, playlists, and other essential entities in a music streaming application. All of this with the implementation of a distributed system using Maven, along with its management for the database created.

## Project Overview

The music streaming platform database project includes the following key components:

1. **Entity-Relationship (ER) Diagram**: A visual representation of the database schema, showing the entities, their attributes, and the relationships between them.

2. **Database Schema**: The detailed SQL code for creating the database schema, including tables, columns, data types, constraints, and relationships.

3. **Sample Data**: A set of sample data that can be used to populate the database and test the application's functionality.

4. **SQL Queries**: A collection of SQL queries that demonstrate how to perform various operations on the database, such as inserting, updating, deleting, and querying data.

5. **Database Management Scripts**: Scripts for managing the database, such as backup, restore, and optimization procedures.

## Getting Started

To get started with the music streaming platform database project, follow these steps:

1. Clone the repository to your local machine:

   ```
   git clone https://github.com/lorenzopaoria/Database-Project-Music-Streaming-Platform.git
   ```

2. Navigate to the cloned repository:

   ```
   cd Database-Project-Music-Streaming-Platform
   ```

3. Review the project files, including the ER diagram, database schema, sample data, and SQL queries.

4. Use the provided SQL scripts to create the database schema and populate it with the sample data.

5. Explore the SQL queries and modify them as needed to perform various operations on the database.

6. Customize the database schema and queries to fit your specific requirements for the music streaming platform.

## User Interface

The music streaming platform project includes a web-based user interface. The interface is divided into the following main sections:

1. **Home Page**:
   - Displays featured artists, new releases, and personalized song recommendations for the logged-in user.
   - Provides search functionality to find songs, artists, and playlists.
   - Allows users to access their personal playlists and listening history.

2. **Browse**:
   - Enables users to browse the complete catalog of artists and songs.
   - Provides filtering and sorting options to refine the search results.
   - Displays detailed information about selected artists and songs.

3. **Playlist Management**:
   - Allows users to create, edit, and delete their own playlists.
   - Provides the ability to add or remove songs from playlists.
   - Enables users to share playlists with other platform members.

4. **Profile**:
   - Lets users view and update their account information, such as name, email, and password.
   - Displays the user's listening history and top artists/genres.
   - Allows users to manage their followed artists and friends.

5. **Administrative Dashboard** (for platform administrators):
   - Provides tools to manage user accounts, including registration, suspension, and role assignment.
   - Enables administrators to add, update, and remove songs, albums, and artist information.
   - Offers analytics and reporting features to monitor platform usage and performance.

The user interface is designed with a clean and modern look, ensuring a seamless and intuitive experience for music enthusiasts. It leverages the Flask web framework, along with Bootstrap for styling and responsive design.

## Maven Installation

To use Maven in your project, you first need to install it. Follow these steps to install Maven:

### 1. Installation on Windows

1. Download the latest version of Maven from [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).
2. Extract the contents of the downloaded archive to a directory of your choice, for example `C:\Program Files\Apache\maven`.
3. Add the `MAVEN_HOME` environment variable pointing to the directory where you extracted Maven (e.g., `C:\Program Files\Apache\maven`).
4. Add `C:\Program Files\Apache\maven\bin` to the `PATH` environment variable.
5. Verify the installation by opening the command prompt and typing:
   ```bash
   mvn -v

You should see the installed version of Maven.

### 2. Running Profiles with Maven

Maven allows you to use profiles to configure various aspects of the build process, such as dependencies, plugins, and properties. To run a specific profile, you can use the mvn command with the -P option. 

First, go in to mvnProject directory and execute the following command to clean and install dependencies:
```
mvn clean install
   ```
To run a profile defined in the pom.xml file, use the following command:
 ```
 mvn -Pserver exec:java
   ```
If you need to run a different profile, such as client, you can specify it like this:

 ```
 mvn -Pclient exec:java
   ```

## Contributing

If you find any issues or have suggestions for improvements, please feel free to submit a pull request or open an issue in the repository.

## License

This project is licensed under the [MIT License](LICENSE).
