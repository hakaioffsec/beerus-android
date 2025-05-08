#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/wait.h>
#include <errno.h>
#include <sys/stat.h>
#include <pthread.h>

#define SOCKET_PATH "/data/data/io.hakaisecurity.beerusframework/beerusd"
#define BUFFER_SIZE 1024
#define MAX_RETRIES 10

void *handle_client(void *arg) {
    int client_socket = *(int *)arg;
    free(arg);

    char buffer[BUFFER_SIZE];
    ssize_t len;

    len = read(client_socket, buffer, sizeof(buffer) - 1);
    if (len <= 0) {
        perror("Failed to read command from client");
        close(client_socket);
        return NULL;
    }

    buffer[len] = '\0';
    printf("Received command: %s\n", buffer);

    FILE *fp = popen(buffer, "r");
    if (fp == NULL) {
        perror("Failed to execute command");
        close(client_socket);
        return NULL;
    }

    while (fgets(buffer, sizeof(buffer), fp) != NULL) {
        if (write(client_socket, buffer, strlen(buffer)) == -1) {
            perror("Failed to send output to client");
            break;
        }
    }

    fclose(fp);
    close(client_socket);
    return NULL;
}

void start_daemon() {
    int server_socket;
    struct sockaddr_un server_addr;
    int retry_count = 0;

    while (retry_count < MAX_RETRIES) {
        server_socket = socket(AF_UNIX, SOCK_STREAM, 0);
        if (server_socket == -1) {
            perror("Failed to create socket");
            exit(EXIT_FAILURE);
        }

        memset(&server_addr, 0, sizeof(struct sockaddr_un));
        server_addr.sun_family = AF_UNIX;
        strncpy(server_addr.sun_path, SOCKET_PATH, sizeof(server_addr.sun_path) - 1);

        unlink(SOCKET_PATH);

        if (bind(server_socket, (struct sockaddr *)&server_addr, sizeof(server_addr)) == -1) {
            perror("Failed to bind socket");
            close(server_socket);
            retry_count++;

            if (retry_count >= MAX_RETRIES) {
                printf("Exceeded maximum retries, exiting...\n");
                exit(EXIT_FAILURE);
            }

            printf("Retrying in 15 seconds...\n");
            sleep(15);
            continue;
        }

        if (chmod(SOCKET_PATH, 0666) == -1) {
            perror("Failed to chmod socket");
            close(server_socket);
            exit(EXIT_FAILURE);
        }

        if (listen(server_socket, 5) == -1) {
            perror("Failed to listen on socket");
            close(server_socket);
            exit(EXIT_FAILURE);
        }

        printf("Daemon started. Waiting for commands...\n");

        while (1) {
            int *client_socket = malloc(sizeof(int));
            if (!client_socket) {
                perror("Failed to allocate memory");
                continue;
            }

            *client_socket = accept(server_socket, NULL, NULL);
            if (*client_socket == -1) {
                perror("Failed to accept client connection");
                free(client_socket);
                continue;
            }

            pthread_t thread_id;
            if (pthread_create(&thread_id, NULL, handle_client, client_socket) != 0) {
                perror("Failed to create thread");
                close(*client_socket);
                free(client_socket);
            } else {
                pthread_detach(thread_id);
            }
        }

        close(server_socket);
        break;
    }
}

int main() {
    setvbuf(stdout, NULL, _IONBF, 0);
    setvbuf(stderr, NULL, _IONBF, 0);

    pid_t pid = fork();
    if (pid < 0) {
        perror("Failed to fork daemon");
        exit(EXIT_FAILURE);
    }
    if (pid > 0) {
        exit(EXIT_SUCCESS);
    }

    if (setsid() < 0) {
        perror("Failed to create a new session");
        exit(EXIT_FAILURE);
    }

    start_daemon();
    return 0;
}