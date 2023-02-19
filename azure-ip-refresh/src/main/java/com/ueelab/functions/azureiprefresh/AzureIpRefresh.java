package com.ueelab.functions.azureiprefresh;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class AzureIpRefresh {
    
    private static final String SCHEDULE = System.getenv("SCHEDULE");
    
    @FunctionName("AzureIpRefresh")
    public void run(@TimerTrigger(name = "timerInfo", schedule = "0 0 23 * * *") String timerInfo, ExecutionContext context) {
        context.getLogger().info("AzureIpRefresh function executed at: " + LocalDateTime.now());
        ipRefreshTask(context);
    }
    
    private static final String CLIENT_ID = System.getenv("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final String TENANT_ID = System.getenv("TENANT_ID");
    private static final String SUBSCRIPTION_ID = System.getenv("SUBSCRIPTION_ID");
    private static final String IP_ID = System.getenv("IP_ID");
    private static final String NETWORK_INTERFACES_ID = System.getenv("NETWORK_INTERFACES_ID");
    
    public void ipRefreshTask(ExecutionContext context) {
        Logger logger = context.getLogger();
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .tenantId(TENANT_ID)
                .build();
        AzureProfile azureProfile = new AzureProfile(AzureEnvironment.AZURE);
        AzureResourceManager azureResourceManager = AzureResourceManager.authenticate(clientSecretCredential, azureProfile).withSubscription(SUBSCRIPTION_ID);
        logger.info("\nSchedule starting ...");
        PublicIpAddress ipAddress = azureResourceManager.publicIpAddresses().getById(IP_ID);
        String beforeIpAddress = ipAddress.ipAddress();
        azureResourceManager.networkInterfaces().getById(NETWORK_INTERFACES_ID)
                .update().withoutPrimaryPublicIPAddress().apply()
                .update().withExistingPrimaryPublicIPAddress(ipAddress).apply();
        logger.info("Before ip address: " + beforeIpAddress);
        logger.info("After  ip address: " + ipAddress.refresh().ipAddress());
    }
    
    public static boolean ping(String host, int port) {
        int connectedCount = 0;
        try(Socket socket = new Socket()) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            for (int i = 0; i < 4; i++) {
                try {
                    socket.connect(inetSocketAddress, 2000);
                    if(socket.isConnected()) connectedCount ++;
                } catch (Exception ignore) {
                
                }
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return connectedCount > 0;
    }
    
    
}
