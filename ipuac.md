# Standard of IPUAC

IPUAC is language to write content in IPU.

## 1. Partitioning

* ### Section

    `==Section name==`  
    Problem content, explain, answer, and more can be separated in section.
    This syntax must be written in a line alone.

## 2. Content

* ### Plain text

    `Plain text`  
    Write text anywhere you want. Text will be rendered as it looks like.
    This can be combined with other content syntax.

* ### Hyperlink

    `[=(URL|explain)]`
    Make hyperlink to URL. For example, `[google.com|Google]` makes next.
    > [Google](google.com)

    If URL and explain text is same, you can simplify like this.
    `[URL]`
    > [google.com](google.com)

    This can be combined with other content syntax.

* ### Mathmetic expressions

    `[$(LaTeX)]`  
    Write [LaTeX](https://www.latex-project.org/) command between '$'. It will be rendered as mathmetic expression.
    This can be combined with other content syntax.

* ### Image

    `[@(Image code)]`  
    Put ImageCode between '[[!' and ']]'.

* ### Table

    ```
    |1|2|3|
    |4|5|6|
    |7|8|9|
    ```

    Use '|' character to put table. To merge cells, for example merge cell 4 and 5, use next.

    ```
    |1|2|3|
    |4/5|6|
    |7|8|9|
    ```

    This syntax must be written in a line alone.

* ### Embeded web view

    `[*(URL)]`
    `[*(URL|alternative text|width|height)]`
    Embed web page using `iframe`.
    height is css property.

* ### Escape character

    `\(Special)`
    Use '\' to express reserved word like '==, [[, | ...'
    This can be combined with other content syntax.

    |Usage|Rendered|
    |-|-|
    |\=|==|
    |\\\||\||
    |\\\\ |\\ |
    |\\[|[|
    |\\]|]|

## 3. Etc

* ## Divide line

    `---`
    Put divide line.
