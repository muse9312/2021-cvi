import { useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { ReviewItem } from '../../components';
import { Frame, LottieAnimation } from '../../components/common';
import { PAGING_SIZE, PATH, RESPONSE_STATE, THEME_COLOR } from '../../constants';
import { useLoading } from '../../hooks';
import {
  Container,
  LottieContainer,
  LoadingContainer,
  ScrollLoadingContainer,
  Title,
  MyReviewList,
  frameStyle,
} from './MyPageReview.styles';
import { useCallback, useEffect, useState } from 'react';
import { getMyReviewListAsync } from '../../service';
import { useInView } from 'react-intersection-observer';
import { NotFoundAnimation } from '../../assets/lotties';

const MyPageReview = () => {
  const history = useHistory();
  const accessToken = useSelector((state) => state.authReducer.accessToken);

  const [myReviewList, setMyReviewList] = useState([]);
  const [offset, setOffset] = useState(0);

  const [ref, inView] = useInView();
  const { showLoading, hideLoading, isLoading, Loading } = useLoading();
  const {
    showLoading: showScrollLoading,
    hideLoading: hideScrollLoading,
    isLoading: isScrollLoading,
    Loading: ScrollLoading,
  } = useLoading();

  const isLastPost = (index) => index === offset + PAGING_SIZE - 1;

  const getMyReviewList = useCallback(async () => {
    if (!accessToken) return;

    const response = await getMyReviewListAsync(accessToken, offset);

    if (response.state === RESPONSE_STATE.FAILURE) {
      alert('failure - getMyReviewListAsync');

      return;
    }

    hideLoading();
    hideScrollLoading();
    setMyReviewList((prevState) => [...prevState, ...response.data]);
  }, [offset, accessToken]);

  const goReviewDetailPage = (id) => {
    history.push(`${PATH.REVIEW}/${id}`);
  };

  useEffect(() => {
    if (offset === 0) {
      showLoading();
    }

    getMyReviewList();
  }, [getMyReviewList]);

  useEffect(() => {
    if (!inView) return;

    setOffset((prevState) => prevState + PAGING_SIZE);
    showScrollLoading();
  }, [inView]);

  return (
    <Container>
      <Title>내가 쓴 글</Title>
      {isLoading ? (
        <LoadingContainer>
          <Loading isLoading={isLoading} backgroundColor={THEME_COLOR.WHITE} />
        </LoadingContainer>
      ) : !myReviewList.length ? (
        <LottieContainer>
          <LottieAnimation
            data={NotFoundAnimation}
            width="26rem"
            mobileWidth="18rem"
            designer="Radhikakpor"
            description="내가 쓴 글이 없습니다"
          />
        </LottieContainer>
      ) : (
        <Frame styles={frameStyle}>
          <MyReviewList>
            {myReviewList?.map((myReview, index) => (
              <ReviewItem
                key={myReview.id}
                review={myReview}
                accessToken={accessToken}
                innerRef={isLastPost(index) ? ref : null}
                onClick={() => goReviewDetailPage(myReview.id)}
              />
            ))}
          </MyReviewList>
        </Frame>
      )}
      {isScrollLoading && (
        <ScrollLoadingContainer>
          <ScrollLoading isLoading={isScrollLoading} width="4rem" height="4rem" />
        </ScrollLoadingContainer>
      )}
    </Container>
  );
};

export default MyPageReview;