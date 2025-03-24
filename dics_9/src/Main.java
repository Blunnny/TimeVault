

/*
Pre-order: 10 3 1 7 12 11 14 13 15
In-order: 1 3 7 10 11 12 13 14 15
Post-order:1 7 3 11 13 15 14 12 10
Level-order (BFS): 10 3 12 1 7 11 14 13 15

begin:
        A
      /   \
     B     C
    / \   / \
   D   E F   G

removeMin()
        C
      /   \
     B     F
    / \   /
   D   E G

insert(X)
        C
      /   \
     B     G
    / \   /
   D   E F
        \
         x
removeMin()
        B
      /   \
     E     G
    / \   /
   D   x F

insert(A)
        A
      /   \
     E     B
    / \   / \
   D   X F   G

<
>
>
<


 */