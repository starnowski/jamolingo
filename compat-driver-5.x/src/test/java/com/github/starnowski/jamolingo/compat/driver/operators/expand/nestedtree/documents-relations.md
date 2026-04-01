## Date
Date: 2026-04-01:12:00:00

## TreeType1 tree:
    TreeType1(_id=10)
    |
    |------->TreeType1(_id=11)
    |        |------->TreeType1(_id=14)
    |        |        |------->TreeType1(_id=21)
    |        |        |------->TreeType1(_id=22)
    |        |        |------->TreeType1(_id=23)
    |        |        |------->TreeType1(_id=24)
    |        |------->TreeType1(_id=15)
    |        |------->TreeType1(_id=16)
    |------->TreeType1(_id=12)
    |        |------->TreeType1(_id=17)
    |        |------->TreeType1(_id=18)
    |        |------->TreeType1(_id=19)
    |------->TreeType1(_id=13)
    |        |------->TreeType1(_id=20)

    TreeType1(_id=1)
    |
    |------->TreeType1(_id=2)
    |        |------->TreeType1(_id=3)
    |        |        |------->TreeType1(_id=4)
    |        |        |        |------->TreeType1(_id=6)
    |        |        |        |        |------->TreeType1(_id=7)
    |        |        |        |        |        |------->TreeType1(_id=8)
    |------->TreeType1(_id=5)

## TreeType2 tree:
    TreeType2(_id=1) (linked to TreeType1 _id=1)
    |
    |------->TreeType2(_id=2)
    |        |------->TreeType2(_id=3)

    TreeType2(_id=4) (linked to TreeType1 _id=2)
    |
    |------->TreeType2(_id=5)
    |        |------->TreeType2(_id=6)

## TreeType3 tree:
    TreeType3(_id=1) (linked to TreeType2 _id=1)
    |
    |------->TreeType3(_id=2)
    |        |------->TreeType3(_id=3)

    TreeType3(_id=4) (linked to TreeType2 _id=4)
    |
    |------->TreeType3(_id=5)
    |        |------->TreeType3(_id=6)

## TreeType4 tree:
    TreeType4(_id=1) (linked to TreeType3 _id=1)
    |
    |------->TreeType4(_id=2)
    |        |------->TreeType4(_id=3)

## Relation between entity types:
    TreeType1(_id=1) --treeType2s--> TreeType2(_id=1)
    TreeType1(_id=1) --treeType2s--> TreeType2(_id=2)
    TreeType1(_id=1) --treeType2s--> TreeType2(_id=3)
    TreeType1(_id=2) --treeType2s--> TreeType2(_id=4)
    TreeType1(_id=2) --treeType2s--> TreeType2(_id=5)
    TreeType1(_id=2) --treeType2s--> TreeType2(_id=6)
    TreeType2(_id=1) --treeType3s--> TreeType3(_id=1)
    TreeType2(_id=1) --treeType3s--> TreeType3(_id=2)
    TreeType2(_id=1) --treeType3s--> TreeType3(_id=3)
    TreeType2(_id=4) --treeType3s--> TreeType3(_id=4)
    TreeType2(_id=4) --treeType3s--> TreeType3(_id=5)
    TreeType2(_id=4) --treeType3s--> TreeType3(_id=6)
    TreeType3(_id=1) --treeType4s--> TreeType4(_id=1)
    TreeType3(_id=1) --treeType4s--> TreeType4(_id=2)
    TreeType3(_id=1) --treeType4s--> TreeType4(_id=3)

## Relation to Category:
    TreeType1(_id=1) --category--> Category(_id=1)
    TreeType1(_id=2) --category--> Category(_id=1)
    TreeType1(_id=3) --category--> Category(_id=2)
    TreeType1(_id=4) --category--> Category(_id=2)
    TreeType1(_id=5) --category--> Category(_id=1)
    TreeType1(_id=6) --category--> Category(_id=2)
    TreeType1(_id=7) --category--> Category(_id=2)
    TreeType1(_id=8) --category--> Category(_id=2)
    TreeType1(_id=10..24) --category--> Category(_id=1)
    TreeType2(_id=1..2, 4..5) --category--> Category(_id=1)
    TreeType2(_id=3, 6) --category--> Category(_id=2)
    TreeType3(_id=1..2, 4..5) --category--> Category(_id=1)
    TreeType3(_id=3, 6) --category--> Category(_id=2)
    TreeType4(_id=1..2) --category--> Category(_id=1)
    TreeType4(_id=3) --category--> Category(_id=2)
