STOP! Are you working on the LSP portion of the SLDS Extension? If not, this repo is not for you. For the VS Code extension code please see the repo for salesforcedx-vscode-slds. That repo contains all of the UI portion of the extension written in JS.

The LSP is the link between the Core validation code and VSCode extension. This is the “Server” part of “Language Server Protocol”. This provides diagnostics, quick fixes, and other functionalities based on recommendations provided by the “Core” code. For more information on LSP’s please see this guide. (https://code.visualstudio.com/api/language-extensions/language-server-extension-guide)

## Structure
*LSP* → This is “Server” part of Language Server Protocol Provides diagnostics, quickfixes, and other functionalities based on recommendations provided by the Core code.

*Core* → Heart of SLDS LSP Parses inputs, performs cross reference check, provides recommendation
