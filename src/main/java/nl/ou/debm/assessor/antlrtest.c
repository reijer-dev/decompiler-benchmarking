#include <stdio.h>

int func_add(int a, int b){
    return a + b;
}

int func_subst(int a, int b){
    return a - b;
}

int main(){
    #define A 10
    #define C 20
    printf("main\n");
    printf("add: %d\n", func_add(A,C));
    printf("subst: %d\n", func_subst(A,C));
    for (int i=0;i<10;++i){
        printf("en dat %d\n", i);
    }
    for (;;){
        break;
    }
    return 0;
}