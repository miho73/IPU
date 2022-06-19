monaco.languages.register({id:'ipuac'});

monaco.languages.setMonarchTokensProvider('ipuac', {
    defaultToken: 'text',
    tokenizer: {
        root: [
            [/!\s.*$/, 'quote'],
            [/^==\s+(.*?)\s+==$/, 'section'],
            [/[']{4}(.*?)[']{4}/, 'bold-n-italic'],
            [/[']{3}(.*?)[']{3}/, 'italic'],
            [/[']{2}(.*?)[']{2}/, 'bold'],
            [/[_]{2}(.*?)[_]{2}/, 'underline'],
            [/[-]{2}(.*?)[-]{2}/, 'strike'],
            [/[\^]{2}(.*?)[\^]{2}/, 'super'],
            [/[,]{2}(.*?)[,]{2}/, 'sub'],
            [/\#def\s.*?=.*/, 'define'],
            [/^\\\d\s/, 'control'],
            [/\[.*?\(.*?\)\]/, 'function'],
            [/-{3}/, 'control'],
            [/\[lf\]/, 'control'],

            // equations
            [/\$([^\$]|\\.)*$/, 'invalid' ], // non-teminated equation
            [/\$/, { token: 'equation', bracket: '@open', next: '@equation' } ],
        ],
      
        equation: [
            [/[^\$]+/, 'equation'],
            [/\$/, { token: 'equation', bracket: '@close', next: '@pop' } ]
        ]
    }
});

monaco.languages.registerCompletionItemProvider('ipuac', {
    provideCompletionItems: () => {
        const suggestions = [
            {
                label: 'separator',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '---'
            },
            {
                label: 'define',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '#def ${1:attribute}=${2:value}',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'section',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '== ${1:title} ==',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'equation',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '$${1:latex}$',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'bold',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '\'\'${1:text}\'\'',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'italic',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '\'\'\'${1:text}\'\'\'',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'bolditalic',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '\'\'\'\'${1:text}\'\'\'\'',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'underline',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '__${1:text}__',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'strike',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '--${1:text}--',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'super',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '^^${1:text}^^',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'sub',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: ',,${1:text},,',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'size',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '{{{${1:size} ${2:text}}}}',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'foreground',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '{{{#${1:color} ${2:text}}}}',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'background',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '[[[#${1:color} ${2:text}]]]',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'url',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '[[${1:url}]]',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'hyperlink',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '[[${1:url}|${2:text}]]',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'imagelocal',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '[{img:${1:resource code}}]',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'image',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '[{${1:source}}]',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'quote',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '! ${1:quote}',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'line',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '[lf]',
            },
            {
                label: 'ytp',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '[ytp(${1:video code})]',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
            {
                label: 'embed',
                kind: monaco.languages.CompletionItemKind.keywords,
                insertText: '[embed(${1:url})]',
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            },
        ];
        return {suggestions: suggestions}
    }
});

monaco.editor.defineTheme('ipuac-theme', {
    base: 'vs-dark',
    inherit: true,
    rules: [
        {token: 'section', foreground: '#A5D3EB'},
        {token: 'bold-n-italic', fontStyle: 'bold italic'},
        {token: 'italic', fontStyle: 'italic'},
        {token: 'bold', fontStyle: 'bold'},
        {token: 'underline', fontStyle: 'underline'},
        {token: 'quote', foreground: '#DCDCDC', fontStyle: 'italic'},
        {token: 'equation', foreground: '#F76C61'},
        {token: 'define', foreground: '#1C8FFF'},
        {token: 'function', foreground: '#1889FC'},
        {token: 'control', foreground: '#ABABAB', fontStyle: 'italic'},
        {token: 'text', foreground: '#F0F0F0'},
        {token: 'invalid', fontStyle: 'underline'}
    ],
    colors: {
		'editor.foreground': '#F0F0F0'
	}
});
function setIpuac(inputValue){
    return {
        value: inputValue,
        language: 'ipuac',
        theme: "ipuac-theme",
        fontFamily: "D2CODING",
        fontSize: 15,
        DefaultEndOfLine: 0,
        lineNumbers: 'on',
        glyphMargin: false,
        vertical: 'auto',
        horizontal: 'auto',
        verticalScrollbarSize: 10,
        horizontalScrollbarSize: 10,
        scrollBeyondLastLine: false,
        automaticLayout: true,
        minimap: {
            enabled: false
        },
        lineHeight: 19
    }
}
var modelUri = monaco.Uri.parse('a://b/foo.json');
monaco.languages.json.jsonDefaults.setDiagnosticsOptions({
    validate: true,
    schemas: [{
        uri: 'answerJson',
		fileMatch: [modelUri.toString()],
        schema: {
            $schema: "http://json-schema.org/draft-07/schema#",
            type: 'array',
            items: [
                {
                    type: 'object',
                    properties: {
                        name: {
                            description: '채점 항목의 이름',
                            type: 'string'
                        },
                        method: {
                            description: '0: 직접채점, 1: 수 채점, 2: 분수 채점',
                            enum: [0, 1, 2]
                        },
                        answer: {
                            description: '정답',
                            type: 'string'
                        },
                    },
                    required: ['name', 'method', 'answer']
                }
            ]
        }
	}],
    allowComments: false
});
function answerJsonModel() {
    var model = monaco.editor.createModel('[]', 'json', modelUri);
    return model;
}