import { useEffect } from 'react';
import { RegionalStateChart, RegionalStateTable, VaccinationState } from '../../components';
import { useFetch, useLoading } from '../../hooks';
import { requestVaccinationStateList } from '../../requests';
import { Container, Title, Content } from './StatePage.styles';

const StatePage = () => {
  const { response, error, loading } = useFetch([], requestVaccinationStateList);
  const { showLoading, hideLoading, isLoading, Loading } = useLoading();

  const regionalState = response.slice(1);

  useEffect(() => {
    loading ? showLoading() : hideLoading();
  }, [loading]);

  return (
    <Container>
      <Title>접종 현황</Title>
      <Content>
        <VaccinationState withWorld={false} />
        <RegionalStateChart
          vaccinationStateList={regionalState}
          isLoading={isLoading}
          Loading={Loading}
        />
        <RegionalStateTable vaccinationStateList={regionalState} />
      </Content>
    </Container>
  );
};

export default StatePage;