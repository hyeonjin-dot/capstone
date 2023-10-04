import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel

import firebase_admin
from firebase_admin import credentials, db
from firebase_admin import auth


cred = credentials.Certificate("/Users/jeonghyeonjin/Desktop/bow/project-hj-4e44f-firebase-adminsdk-os9vw-11a24a229b.json")
firebase_admin.initialize_app(cred, {'databaseURL': 'https://project-hj-4e44f-default-rtdb.firebaseio.com/'})

user = auth.get_user_by_email("teseuteu593@gmail.com")  # 사용자 이메일을 지정
uid = user.uid

# 데이터 가져오기
ref = db.reference(f'{uid}')
data = ref.get()
test_list = [[item['first'], str(item['second'])] for item in data]

df = pd.read_csv('raw/output_file.csv')

tfidf = TfidfVectorizer()

df['줄거리'] = df['줄거리'].fillna('')

tfidf_matrix = tfidf.fit_transform(df['줄거리'])

cos_sim = linear_kernel(tfidf_matrix, tfidf_matrix)

indices = pd.Series(df.index, df['이름']).drop_duplicates()

def get_recommandation(info_list, feedback_weights, cos_sim=cos_sim):
    tmp_list = []
    res_list = []
    for t_list in info_list:
        idx = indices[t_list[0]]
                
        sim_scores = list(enumerate(cos_sim[idx]))
        
        sim_scores = sorted(sim_scores, key = lambda x:x[1], reverse=True)
        sim_scores = sim_scores[1:11]
        musical_indices = [i[0] for i in sim_scores]
        
        for i in range(10):
            musical_name = df['이름'].iloc[musical_indices[i]]
            idx, similarity = sim_scores[i]
            similarity = similarity * (float(t_list[1])/100)
            
            if feedback_weights and musical_name in feedback_weights:
                similarity *= feedback_weights[musical_name]

            ck_idx = find_index_in_list(tmp_list, musical_name)
            if (ck_idx != -1):
                tmp_list[ck_idx][2] += similarity
            else:
                tmp_list.append([musical_name,idx,similarity])
    
    tmp_list = sorted(tmp_list, key=lambda x: x[2], reverse=True)  # similarity를 기준으로 정렬
    tmp_list = tmp_list[:5] #나중에 가공할거면 그냥 tmp_list넘겨주면 댐
    for i in tmp_list:
        res_list.append(i[0])
    return res_list


def find_index_in_list(list_to_search, target_string):
    for idx, element in enumerate(list_to_search):
        if element[0] == target_string:
            return idx
    return -1

def feedback_loop():
    feedback_weights = {}  # 각 아이템에 대한 피드백 가중치 저장
    
    while True:
        # 사용자로부터 피드백을 입력 받습니다.
        user_feedback = input("추천에 대한 피드백을 입력하세요 (아이템 이름 및 가중치, 예: '뮤지컬1 0.8'):")
        if user_feedback == 'exit':
            break
        
        musical_name, weight = user_feedback.split()
        musical_name = musical_name.strip("'")
        weight = weight.strip("'")
        feedback_weights[musical_name] = (float(weight))
        
        # 가중 평균을 기반으로 새로운 추천을 생성합니다.
        # 기존 리스트 가져오는 방법 생각하기
        recommended_musicals = get_recommandation(test_list, feedback_weights)
        print(f"새로운 추천: {recommended_musicals}")


a = get_recommandation(test_list,[])
print(a)
#feedback_loop()