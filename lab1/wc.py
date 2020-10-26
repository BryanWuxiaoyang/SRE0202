# -*- coding: utf-8 -*-
from wordcloud import WordCloud
f = open("./reviews.txt", "r", encoding='utf-8')
data = f.read()
w = WordCloud(width=800,height=600,background_color='white')
w.generate(data)
w.to_file("./wordcloud.png")
