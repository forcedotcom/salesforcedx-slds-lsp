/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp;

import com.salesforce.slds.lsp.configuration.ServerConfiguration;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@SpringBootApplication
@Import(ServerConfiguration.class)
public class ServerLauncher implements InitializingBean {

    @Autowired
    Server server;

    @Value("${PORT}")
    String port;

    @Override
    public void afterPropertiesSet() {
        Socket socket = null;
        try {
            socket = new Socket("localhost", Integer.parseInt(port));

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);

            launcher.startListening();
        } catch (Exception e) {
            System.err.print(e);
        }
    }
    
    public static void main(String[] args){
        SpringApplication.run(ServerLauncher.class, args);
    }


}