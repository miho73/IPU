# Standard of IPUAC

IPUAC is language to write content in IPU.

## 1. Organize text

* ### Section

    `==Section name==`  
    Problem content, explain, answer, and more can be separated in section.
    This syntax must be written in a line alone.

* ### Text alignment

    `\1`, `\2`, `\3`, `\4`  
    Put one of above in fron of a line. These will set text alignmnet of line.
    |IPUAC|Alignment|
    |-|-|
    |`\1`|left|
    |`\2`|center|
    |`\3`|right|
    |`\4`|Justify|

    Or, you can set default text alignment. Put below at the top.  
    `#def text-alignment=[alignment number]`  
    For example, next set default alignment to *justify*  
    `#def text-alignment=4`  

* ## New line

    As default, all new line(Enter) is ignored. To put a new line put next in a line alone.  
    `#`

## 2. Content

* ### Plain text

    `Plain text`  
    Write text anywhere you want. Text will be rendered as it looks like.  
    This can be combined with other content syntax.

* ### Hyperlink

    `[=(URL|explain)]`
    Make hyperlink to URL. For example, `[https://google.com|Google]` makes next.
    > [Google](https://google.com)

    If URL and explain text is same, you can simplify like this.
    `[=(URL)]]`
    > [https://google.com](https://google.com)

    This can be combined with other content syntax.

* ### Mathmetic expressions

    `[$(LaTeX)]`  
    Write [LaTeX](https://www.latex-project.org/) command. It will be rendered as mathmetic expression.  
    This can be combined with other content syntax.

* ### Image

    `[@(Image code)]`  
    `[@(Image code|Image alternative)]`  
    `[@(Image code|Image alternative|width|heigth)]`  
    Put ImageCode between '[@[' and ']]'.

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
    |\\#|#|

## 3. Etc

* ## Divide line

    `---`  
    Put divide line.
