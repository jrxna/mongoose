<picture>
  <source srcset="./images/mongoose.png">
  <img alt="Mongoose" src="./images/mongoose.png">
  <br />
</picture>


# Mongoose Static Site Generator

Mongoose is a simple and efficient static site generator built with Java and Spring Boot. It converts your Markdown files into a fully functional static website, complete with custom templates and styling.

## Features

- **Markdown to HTML Conversion**: Easily convert your Markdown files to static HTML pages.
- **Customizable Templates**: Use your own HTML templates for different pages.
- **Asset Management**: Include custom CSS, fonts, and images.
- **Automatic Blog Generation**: Generate a blog with individual posts and listings.
- **Easy Configuration**: Define input and output directories as command-line arguments.

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Gradle 8.3** or higher (recommend using [SDKMAN!](https://sdkman.io/) for installation)

### Installation

1. **Clone the Repository**

   ```bash
   git clone https://github.com/jrxna/mongoose.git
   cd mongoose
   ```

2. **Build the Project**

   ```bash
   gradle clean build
   ```

   *Alternatively, use the Gradle Wrapper:*

   ```bash
   ./gradlew clean build
   ```

### Usage

Run the application by specifying the input and output directories:

```bash
java -jar build/libs/mongoose.jar /path/to/input /path/to/output
```

- `/path/to/input`: Directory containing your Markdown files.
- `/path/to/output`: Directory where the generated site will be placed.

### Directory Structure

- **input/**: Contains your Markdown content.
  - `about.md`
  - `projects.md`
  - `posts/`
    - `post1.md`
    - `post2.md`
- **static-site-template/**: Contains your HTML templates and assets.
  - `index.html`
  - `projects.html`
  - `blog.html`
  - `styles.css`
  - `images/`
    - `logo.png` *(Your logo image)*
    - Other images
  - `fonts/`
    - Font files

### Customization

- **Templates**: Modify the HTML files in `static-site-template/` to change the site's structure and layout.
- **Styles**: Edit `styles.css` and add your own styles.
- **Content**: Update the Markdown files in the `input/` directory with your content.

### Example

1. **Create Your Content**

   - Write your `about.md`, `projects.md`, and blog posts in the `posts/` directory.

2. **Run the Generator**

   ```bash
   java -jar build/libs/mongoose.jar input output
   ```

3. **View Your Site**

   - Open `output/index.html` in your web browser to see your generated site.

### Contributing

Contributions are welcome! If you have suggestions for improvements or find bugs, please open an issue or submit a pull request.

### License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

### Acknowledgments

- [Flexmark-java](https://github.com/vsch/flexmark-java) for Markdown parsing.
- [Spring Boot](https://spring.io/projects/spring-boot) for the application framework.
- [SDKMAN!](https://sdkman.io/) for managing Java and Gradle versions.