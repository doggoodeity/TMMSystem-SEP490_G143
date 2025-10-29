import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Button, Alert, Spinner } from 'react-bootstrap';
import { FaArrowLeft, FaPaperPlane, FaCheck } from 'react-icons/fa';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../../components/common/Header';
import InternalSidebar from '../../components/common/InternalSidebar';
import { quoteService } from '../../api/quoteService';
import { productService } from '../../api/productService';
import '../../styles/RFQDetail.css';

const RFQDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [rfqData, setRFQData] = useState(null);
  const [customerData, setCustomerData] = useState(null);
  const [productMap, setProductMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [sendingRFQ, setSendingRFQ] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const fetchRFQDetails = async () => {
      setLoading(true);
      try {
        console.log('=== FETCHING RFQ DETAILS ===');
        console.log('RFQ ID:', id);

        // Fetch RFQ details
        const realRFQ = await quoteService.getRFQDetails(id);
        console.log('Real RFQ data:', realRFQ);
        setRFQData(realRFQ);
        
        // Fetch all customers to get customer data
        const customers = await quoteService.getAllCustomers();
        console.log('All customers:', customers);
        const customer = customers.find(c => c.id === realRFQ.customerId);
        console.log('Found customer:', customer);
        setCustomerData(customer);
        
        // Fetch all products to map product IDs to names
        const products = await productService.getAllProducts();
        console.log('All products:', products);
        const prodMap = {};
        products.forEach(product => {
          prodMap[product.id] = product;
        });
        setProductMap(prodMap);
        
      } catch (error) {
        console.error('Error fetching RFQ details:', error);
        setError('Không thể tải chi tiết RFQ. Vui lòng thử lại.');
      } finally {
        setLoading(false);
      }
    };

    fetchRFQDetails();
  }, [id]);

  const handleGoBack = () => {
    navigate('/internal/quote-requests');
  };

  const handleSendRFQ = async () => {
    if (!rfqData) {
      setError('Không tìm thấy thông tin RFQ để gửi.');
      return;
    }

    if ([
      'FORWARDED_TO_PLANNING',
      'RECEIVED_BY_PLANNING',
      'QUOTED',
      'APPROVED',
      'REJECTED',
      'CANCELED'
    ].includes(rfqData.status)) {
      setError('RFQ này không thể gửi lại vì đã được xử lý trong quy trình tiếp theo.');
      return;
    }

    setSendingRFQ(true);
    setError('');
    setSuccess('');

    try {
      console.log('=== START RFQ FORWARDING WORKFLOW ===');
      let latestData = { ...rfqData };
      const completedSteps = [];

      if (latestData.status === 'DRAFT') {
        console.log('RFQ hiện ở trạng thái DRAFT → gọi /send');
        const sentResult = await quoteService.sendRfq(id);
        latestData = { ...latestData, ...sentResult };
        completedSteps.push('Gửi yêu cầu');
      }

      if (latestData.status === 'SENT') {
        console.log('RFQ hiện ở trạng thái SENT → gọi /preliminary-check');
        const checkedResult = await quoteService.preliminaryCheck(id);
        latestData = { ...latestData, ...checkedResult };
        completedSteps.push('Kiểm tra sơ bộ');
      }

      if (latestData.status === 'PRELIMINARY_CHECKED') {
        console.log('RFQ hiện ở trạng thái PRELIMINARY_CHECKED → gọi /forward-to-planning');
        const forwardedResult = await quoteService.sendRFQToPlanningDepartment(id);
        latestData = { ...latestData, ...forwardedResult };
        completedSteps.push('Chuyển đến Phòng Kế hoạch');
      }

      // Refresh data from backend to make sure we show the latest workflow status
      try {
        const refreshedData = await quoteService.getRFQDetails(id);
        if (refreshedData) {
          latestData = refreshedData;
        }
      } catch (refreshError) {
        console.warn('Không thể làm mới dữ liệu RFQ sau khi gửi:', refreshError);
      }

      setRFQData(latestData);

      if (latestData.status === 'FORWARDED_TO_PLANNING' || latestData.status === 'RECEIVED_BY_PLANNING') {
        const stepsDescription = completedSteps.length ? ` (các bước: ${completedSteps.join(' → ')})` : '';
        setSuccess(`✅ RFQ đã được chuyển thành công đến Phòng Kế hoạch${stepsDescription}.`);
      } else {
        setSuccess('✅ RFQ đã được xử lý thành công.');
      }

      setTimeout(() => {
        setSuccess('');
      }, 5000);
    } catch (error) {
      console.error('Send RFQ error:', error);
      setError('❌ ' + (error.message || 'Lỗi khi gửi RFQ đến Phòng Kế hoạch'));
    } finally {
      setSendingRFQ(false);
    }
  };

  const getStatusDisplay = (status) => {
    switch (status) {
      case 'DRAFT':
        return 'Chờ xử lý';
      case 'SENT':
        return 'Đã gửi';
      case 'PRELIMINARY_CHECKED':
        return 'Đã kiểm tra sơ bộ';
      case 'FORWARDED_TO_PLANNING':
        return 'Đã chuyển kế hoạch';
      case 'RECEIVED_BY_PLANNING':
        return 'Phòng Kế hoạch đã nhận';
      case 'QUOTED':
        return 'Đã báo giá';
      case 'APPROVED':
        return 'Đã duyệt';
      case 'REJECTED':
        return 'Từ chối';
      case 'CANCELED':
        return 'Đã hủy';
      default:
        return status || 'Chờ xử lý';
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'DRAFT':
        return 'warning';
      case 'SENT':
        return 'primary';
      case 'PRELIMINARY_CHECKED':
        return 'info';
      case 'FORWARDED_TO_PLANNING':
      case 'RECEIVED_BY_PLANNING':
        return 'secondary';
      case 'QUOTED':
        return 'info';
      case 'APPROVED':
        return 'success';
      case 'REJECTED':
        return 'danger';
      case 'CANCELED':
        return 'dark';
      default:
        return 'secondary';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      return new Date(dateString).toLocaleDateString('vi-VN');
    } catch {
      return dateString;
    }
  };

  const canSendRFQ = () => {
    if (!rfqData) return false;
    return ['DRAFT', 'SENT', 'PRELIMINARY_CHECKED'].includes(rfqData.status);
  };

  if (loading) {
    return (
      <div className="internal-layout">
        <Header />
        <div className="d-flex">
          <InternalSidebar />
          <div className="flex-grow-1 d-flex justify-content-center align-items-center" style={{ minHeight: 'calc(100vh - 70px)' }}>
            <div>Đang tải dữ liệu...</div>
          </div>
        </div>
      </div>
    );
  }

  if (error && !rfqData) {
    return (
      <div className="internal-layout">
        <Header />
        <div className="d-flex">
          <InternalSidebar />
          <div className="flex-grow-1 d-flex justify-content-center align-items-center" style={{ minHeight: 'calc(100vh - 70px)' }}>
            <div className="text-danger">{error}</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="internal-layout">
      <Header />
      
      <div className="d-flex">
        <InternalSidebar />
        
        <div className="flex-grow-1" style={{ backgroundColor: '#f8f9fa', minHeight: 'calc(100vh - 70px)' }}>
          <Container fluid className="p-4">
            <div className="rfq-detail-page">
              {/* Status Messages */}
              {error && (
                <Alert variant="danger" dismissible onClose={() => setError('')}>
                  {error}
                </Alert>
              )}
              
              {success && (
                <Alert variant="success" dismissible onClose={() => setSuccess('')}>
                  {success}
                </Alert>
              )}

              {/* Page Header */}
              <div className="page-header mb-4 d-flex justify-content-between align-items-center">
                <h1 className="page-title">Chi tiết Yêu cầu Báo giá</h1>
                <div className="header-actions">
                  <Button
                    variant="outline-secondary"
                    onClick={handleGoBack}
                    className="me-3"
                  >
                    <FaArrowLeft className="me-2" />
                    Quay lại danh sách
                  </Button>
                  
                  {canSendRFQ() ? (
                    <Button
                      variant="dark"
                      onClick={handleSendRFQ}
                      disabled={sendingRFQ}
                    >
                      {sendingRFQ ? (
                        <>
                          <Spinner as="span" animation="border" size="sm" role="status" className="me-2" />
                          Đang gửi...
                        </>
                      ) : (
                        <>
                          <FaPaperPlane className="me-2" />
                          Gửi RFQ
                        </>
                      )}
                    </Button>
                  ) : (
                    <Button variant="success" disabled>
                      <FaCheck className="me-2" />
                      Đã gửi
                    </Button>
                  )}
                </div>
              </div>

              {/* Customer and RFQ Info Cards */}
              <Row className="mb-4">
                {/* Customer Information */}
                <Col lg={6}>
                  <Card className="info-card shadow-sm h-100">
                    <Card.Header className="bg-primary text-white">
                      <h5 className="mb-0">Thông tin khách hàng</h5>
                    </Card.Header>
                    <Card.Body className="p-4">
                      <div className="info-item">
                        <strong>Tên khách hàng:</strong> {customerData?.contactPerson || 'N/A'}
                      </div>
                      <div className="info-item">
                        <strong>Công ty:</strong> {customerData?.companyName || 'N/A'}
                      </div>
                      <div className="info-item">
                        <strong>Email:</strong> {customerData?.email || 'N/A'}
                      </div>
                      <div className="info-item">
                        <strong>Điện thoại:</strong> {customerData?.phoneNumber || 'N/A'}
                      </div>
                      <div className="info-item">
                        <strong>Mã số thuế:</strong> {customerData?.taxCode || 'N/A'}
                      </div>
                    </Card.Body>
                  </Card>
                </Col>

                {/* RFQ Information */}
                <Col lg={6}>
                  <Card className="info-card shadow-sm h-100">
                    <Card.Header className="bg-primary text-white">
                      <h5 className="mb-0">Thông tin RFQ</h5>
                    </Card.Header>
                    <Card.Body className="p-4">
                      <div className="info-item">
                        <strong>Mã RFQ:</strong> {rfqData?.rfqNumber || 'N/A'}
                      </div>
                      <div className="info-item">
                        <strong>Ngày tạo:</strong> {formatDate(rfqData?.createdAt)}
                      </div>
                      <div className="info-item">
                        <strong>Trạng thái:</strong> 
                        <span className={`badge bg-${getStatusColor(rfqData?.status)} ms-2`}>
                          {getStatusDisplay(rfqData?.status)}
                        </span>
                      </div>
                      <div className="info-item">
                        <strong>Ngày mong muốn nhận:</strong> {formatDate(rfqData?.expectedDeliveryDate)}
                      </div>
                      <div className="info-item">
                        <strong>Số lượng sản phẩm:</strong> {rfqData?.details?.length || 0}
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              </Row>

              {/* Product Details Table */}
              <Card className="products-card shadow-sm">
                <Card.Header className="bg-primary text-white">
                  <h5 className="mb-0">Danh sách sản phẩm</h5>
                </Card.Header>
                <Card.Body className="p-0">
                  <Table responsive className="products-table mb-0">
                    <thead className="table-header">
                      <tr>
                        <th style={{ width: '80px' }}>STT</th>
                        <th style={{ width: '150px' }}>Mã đơn hàng</th>
                        <th style={{ minWidth: '200px' }}>Sản phẩm</th>
                        <th style={{ width: '120px' }}>Kích thước</th>
                        <th style={{ width: '100px' }}>Số lượng</th>
                      </tr>
                    </thead>
                    <tbody>
                      {rfqData?.details?.length > 0 ? rfqData.details.map((item, index) => {
                        const product = productMap[item.productId];
                        return (
                          <tr key={item.id || index}>
                            <td className="text-center">{index + 1}</td>
                            <td>
                              <span className="order-code">ORD-{String(index + 1).padStart(3, '0')}</span>
                            </td>
                            <td>
                              <span className="product-name">
                                {product?.name || `Sản phẩm ID: ${item.productId}`}
                              </span>
                            </td>
                            <td className="text-center">
                              <span className="size-info">
                                {item.notes || item.size || product?.standardDimensions || 'N/A'}
                              </span>
                            </td>
                            <td className="text-center">
                              <span className="quantity">{item.quantity}</span>
                            </td>
                          </tr>
                        );
                      }) : (
                        <tr>
                          <td colSpan="5" className="text-center py-4">
                            Không có dữ liệu sản phẩm
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            </div>
          </Container>
        </div>
      </div>
    </div>
  );
};

export default RFQDetail;
